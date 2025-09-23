package fr.gamegauge.gamegauge_api.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class BoardOrderUpdateRequest {
    private List<Long> boardIds;
}