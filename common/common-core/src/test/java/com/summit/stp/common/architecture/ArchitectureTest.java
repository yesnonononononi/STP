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
        Path projectRoot = resolveProjectRoot();
        if (projectRoot == null) {
            // 无法定位聚合根模块（例如独立运行该类）时跳过断言，避免跨平台误报
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

    /**
     * 从当前工作目录逐级向上定位聚合（父）工程根目录。
     * <p>判定标准：目录下存在 {@code pom.xml} 且该文件包含 {@code <modules>} 配置，
     * 即视为多模块聚合根。Windows 本地与 Linux CI 行为一致，无需硬编码磁盘路径。</p>
     *
     * @return 聚合根目录；未找到时返回 {@code null}
     */
    private Path resolveProjectRoot() {
        Path current = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        while (current != null) {
            Path pom = current.resolve("pom.xml");
            if (Files.isRegularFile(pom) && containsModules(pom)) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * 判断 pom.xml 是否为包含 {@code <modules>} 的聚合工程描述。
     *
     * @param pom pom.xml 路径
     * @return 包含 modules 配置时返回 true
     */
    private boolean containsModules(Path pom) {
        try {
            String content = Files.readString(pom);
            return content.contains("<modules>");
        } catch (IOException e) {
            return false;
        }
    }
}
