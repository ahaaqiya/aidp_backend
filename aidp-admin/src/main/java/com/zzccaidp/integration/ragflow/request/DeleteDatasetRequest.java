package com.zzccaidp.integration.ragflow.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DeleteDatasetRequest {

    private List<String> ids;
}