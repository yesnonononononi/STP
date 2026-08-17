package com.summit.stp.common.architecture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ArchitectureTest {

    @Test
    @DisplayName("检查项目源码中 Domain 层文件不应非法直接 import Infrastructure 层包")
    public void domainLayerImportsCheck() throws IOException {
        Path projectRoot = Paths.get("d:/Code/STP");
        if (!Files.exists(projectRoot)) {
            return;
        }

        try (var stream = Files.walk(projectRoot)) {
            List<Path> domainFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> p.toString().contains(File.separator + "domain" + File.separator))
                    .toList();

            for (Path path : domainFiles) {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    if (line.startsWith("import ")) {
                        Assertions.assertFalse(
                                line.contains(".infrastructure.") && !line.contains(".infrastructure.persistence.po"),
                                "Domain 层文件不允许依赖 Infrastructure: " + path.getFileName() + " -> " + line
                        );
                    }
                }
            }
        }
    }
}
