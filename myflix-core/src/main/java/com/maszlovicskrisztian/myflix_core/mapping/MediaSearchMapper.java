package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaSearchResponse;
import com.maszlovicskrisztian.myflix_core.model.MediaType;
import com.maszlovicskrisztian.myflix_core.repository.projection.TitleProjection;
import org.springframework.stereotype.Component;

@Component
public class MediaSearchMapper {
    public MediaSearchResponse fromShow(TitleProjection projection)  {
        return new MediaSearchResponse(projection.getId(), projection.getTitle(), MediaType.TV.name());
    }

    public MediaSearchResponse fromMovie(TitleProjection projection)  {
        return new MediaSearchResponse(projection.getId(), projection.getTitle(), MediaType.MOVIE.name());
    }
}
