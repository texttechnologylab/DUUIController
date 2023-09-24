package api.io;

import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIExternalFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileReader {

    public static List<DUUIExternalFile> getFiles(String sourceDirectory) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(sourceDirectory))) {
            return stream.filter(Files::isRegularFile).map(
                (path -> {
                    try (InputStream inputStream = new FileInputStream(path.toFile())) {
                        return new DUUIExternalFile(
                            path.getFileName().toString(),
                            path.toString(),
                            path.toFile().length(),
                            new ByteArrayInputStream(inputStream.readAllBytes()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
            ).collect(Collectors.toList());
        }
    }

    public static void main(String[] args) throws IOException {
        FileReader.getFiles("D:\\Uni Informatik B.sc\\Bachelor\\DUUIController\\sample").forEach(
            (duuiExternalFile -> System.out.println(duuiExternalFile.getSizeBytes()))
        );
    }
}
