package org.redhat.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JMXOperation {
    String type;
    String mbean;
    String operation;
    String[] arguments;
}
