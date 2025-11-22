package com.furtim.entitleguard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileResponseDto {

	private String id;
	private String fileName;
	private String fileUrl;
	private String mapId;

}
