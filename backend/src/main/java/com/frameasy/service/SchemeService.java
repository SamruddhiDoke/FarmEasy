package com.frameasy.service;

import com.frameasy.model.Scheme;
import com.frameasy.repository.SchemeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

/**
 * Fetches government schemes from data.gov.in (or configurable API) and caches in DB.
 * Scheduler refreshes every 24 hours. Offline: last fetched schemes remain in DB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemeService {

    private final SchemeRepository schemeRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${schemes.api-url:https://api.data.gov.in/resource}")
    private String apiUrl;

    @Value("${schemes.api-key:}")
    private String apiKey;

    @Transactional(readOnly = true)
    public List<Scheme> listAll() {
        return schemeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Scheme> listByState(String state) {
        if (state == null || state.isBlank()) return schemeRepository.findAll();
        return schemeRepository.findByStateContainingIgnoreCase(state);
    }

    @Transactional(readOnly = true)
    public List<Scheme> search(String query) {
        if (query == null || query.isBlank()) return schemeRepository.findAll();
        return searchInList(schemeRepository.findAll(), query);
    }

    public List<Scheme> searchInList(List<Scheme> list, String query) {
        if (query == null || query.isBlank()) return list;
        String q = query.toLowerCase();
        return list.stream()
                .filter(s -> (s.getTitle() != null && s.getTitle().toLowerCase().contains(q))
                        || (s.getSummary() != null && s.getSummary().toLowerCase().contains(q))
                        || (s.getState() != null && s.getState().toLowerCase().contains(q)))
                .toList();
    }

    /**
     * Fetch from external API and save to DB. Called by scheduler or admin manual refresh.
     */
    @Transactional
    public int fetchAndCache() {
        try {
            // data.gov.in often uses resource IDs; we use a generic fetch that can be configured
            String url = apiUrl;
            if (apiKey != null && !apiKey.isBlank()) {
                url += (url.contains("?") ? "&" : "?") + "api-key=" + apiKey;
            }
            String json = restTemplate.getForObject(url, String.class);
            if (json == null || json.isBlank()) {
                seedSampleSchemesIfEmpty();
                return 0;
            }
            return parseAndSave(json);
        } catch (Exception e) {
            log.warn("Scheme fetch failed, using cached data: {}", e.getMessage());
            seedSampleSchemesIfEmpty();
            return 0;
        }
    }

    private int parseAndSave(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        List<Scheme> schemes = new ArrayList<>();
        JsonNode records = root.has("records") ? root.get("records") : root.has("data") ? root.get("data") : root;
        if (records.isArray()) {
            for (JsonNode rec : records) {
                Scheme s = new Scheme();
                s.setExternalId(rec.has("id") ? rec.get("id").asText() : null);
                s.setTitle(rec.has("title") ? rec.get("title").asText() : rec.has("scheme_name") ? rec.get("scheme_name").asText() : "Scheme");
                s.setSummary(rec.has("summary") ? rec.get("summary").asText() : rec.has("description") ? rec.get("description").asText() : null);
                s.setEligibility(rec.has("eligibility") ? rec.get("eligibility").asText() : null);
                s.setTimeline(rec.has("timeline") ? rec.get("timeline").asText() : rec.has("last_date") ? rec.get("last_date").asText() : null);
                s.setState(rec.has("state") ? rec.get("state").asText() : null);
                s.setOfficialUrl(rec.has("official_url") ? rec.get("official_url").asText() : rec.has("link") ? rec.get("link").asText() : null);
                s.setRawJson(rec.toString());
                s.setFetchedAt(Instant.now());
                schemes.add(s);
            }
        }
        if (!schemes.isEmpty()) {
            schemeRepository.deleteAll();
            schemeRepository.saveAll(schemes);
        }
        return schemes.size();
    }

    private void seedSampleSchemesIfEmpty() {
        if (schemeRepository.count() > 0) return;
        List<Scheme> sample = List.of(
                scheme("PM-KISAN", "Income support of ₹6000/year to eligible farmer families in three equal instalments.", "Small and marginal farmers with cultivable land up to 2 hectares", "2024-25", "All India", "https://pmkisan.gov.in"),
                scheme("PM Fasal Bima Yojana", "Crop insurance scheme to provide financial support to farmers in case of crop failure.", "All farmers growing notified crops", "Ongoing", "All India", "https://pmfby.gov.in"),
                scheme("Soil Health Card Scheme", "Issues soil health cards to farmers every 2 years with nutrient status and recommendations.", "All farmers", "2015-ongoing", "All India", "https://soilhealth.dac.gov.in"),
                scheme("Kisan Credit Card", "Providing adequate and timely credit for agricultural needs including crop production.", "Individual farmers, Joint borrowers, Tenant farmers", "Ongoing", "All India", "https://www.nabard.org"),
                scheme("National Mission on Sustainable Agriculture", "Promotes sustainable agriculture through climate-resilient practices.", "Farmers adopting sustainable practices", "2014-ongoing", "All India", "https://nmsa.dac.gov.in"),
                scheme("Pradhan Mantri Krishi Sinchai Yojana", "Har Khet Ko Pani - Ensuring water conservation and irrigation efficiency.", "Farmers with irrigable land", "2015-ongoing", "All India", "https://pmksy.gov.in"),
                scheme("Paramparagat Krishi Vikas Yojana", "Promotion of organic farming and certification.", "Farmers practicing organic farming", "2015-ongoing", "All India", "https://pgsindia-ncof.gov.in"),
                scheme("e-NAM", "National Agriculture Market - electronic trading platform for agricultural commodities.", "Farmers, Traders, FPOs", "2016-ongoing", "All India", "https://enam.gov.in"),
                scheme("Rashtriya Krishi Vikas Yojana", "Incentivizing states to increase public investment in agriculture.", "State governments, Farmers", "Ongoing", "All India", "https://rkvy.nic.in"),
                scheme("Agri Infrastructure Fund", "Medium to long term debt financing for viable post-harvest management and community farming assets.", "FPOs, PACS, entrepreneurs", "2020-2032", "All India", "https://agriinfra.dac.gov.in"),
                scheme("Dairy Entrepreneurship Development Scheme", "Creation of self-employment through dairy sector.", "Individuals, SHGs, Dairy cooperatives", "Ongoing", "All India", "https://www.nddb.coop"),
                scheme("National Livestock Mission", "Sustainable development of livestock sector.", "Livestock farmers, SHGs", "2014-ongoing", "All India", "https://dahd.nic.in"),
                scheme("Agriculture Export Policy", "Promoting agricultural exports and doubling farmers' income.", "Export-oriented farmers, Agri-exporters", "2018-ongoing", "All India", "https://apeda.gov.in"),
                scheme("Mission Organic Value Chain Development", "Development of certified organic production in value chain mode.", "Organic farmers, FPOs", "2017-ongoing", "North East India", "https://movcd.nic.in"),
                scheme("Micro Irrigation Fund", "Promoting micro-irrigation for water use efficiency.", "Farmers adopting drip/sprinkler irrigation", "2018-ongoing", "All India", "https://pmksy.gov.in"),
                scheme("Pradhan Mantri Kisan Maan Dhan Yojana", "Pension scheme for small and marginal farmers.", "Farmers aged 18-40 with land up to 2 hectares", "2019-ongoing", "All India", "https://pmkmy.gov.in")
        );
        schemeRepository.saveAll(sample);
    }

    private Scheme scheme(String title, String summary, String eligibility, String timeline, String state, String url) {
        Scheme s = new Scheme();
        s.setTitle(title);
        s.setSummary(summary);
        s.setEligibility(eligibility);
        s.setTimeline(timeline);
        s.setState(state);
        s.setOfficialUrl(url);
        s.setFetchedAt(Instant.now());
        return s;
    }

    @Transactional
    public void refreshCache() {
        fetchAndCache();
    }

    @Transactional
    public void seedIfEmpty() {
        if (schemeRepository.count() < 15) {
            schemeRepository.deleteAll();
            seedSampleSchemesIfEmpty();
        }
    }
}
