package com.xtremand.custom.css.dto;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class ThemeThemePropertiesListWrapper {
  ThemeDTO themeDto = new ThemeDTO();
  Set<ThemePropertiesDTO> propertiesList = new HashSet<ThemePropertiesDTO>();
}
