package com.zzccaidp.vo.ai;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FileRecognitionIn {

  @NotNull(message = "flowid cannot be null")
  private String flowid;

  @NotNull(message = "fileId cannot be null")
  private String fileId;

  @NotNull(message = "ruleId cannot be null")
  private String ruleId;

  @NotNull(message = "chl cannot be null")
  private String chl;

}
