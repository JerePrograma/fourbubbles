package ar.com.ropalista.catalog.api;

import ar.com.ropalista.catalog.application.CatalogQueryService;
import ar.com.ropalista.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {
    private final CatalogQueryService queries;

    public CatalogController(CatalogQueryService queries) {
        this.queries = queries;
    }

    @GetMapping("/equivalences")
    ApiResponse<List<CatalogQueryService.EquivalenceView>> equivalences() {
        return ApiResponse.ok(queries.equivalences());
    }

    @GetMapping("/services")
    ApiResponse<List<CatalogQueryService.ServiceView>> services() {
        return ApiResponse.ok(queries.services());
    }
}
