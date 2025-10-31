package com.waters.punchout.service;

import com.waters.punchout.model.CxmlRequest;
import com.waters.punchout.model.ConversionResponse;

public interface CxmlConversionService {
    ConversionResponse convertCxml(CxmlRequest request);
}
