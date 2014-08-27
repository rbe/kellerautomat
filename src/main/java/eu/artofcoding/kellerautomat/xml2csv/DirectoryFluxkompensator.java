package eu.artofcoding.kellerautomat.xml2csv;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DirectoryFluxkompensator {

    private List<LoggerCallback> loggerCallbackList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.format("Please give working directory as first argument%n");
            System.out.format("usage: java %s <working directory>%n", DirectoryFluxkompensator.class.getName());
            System.exit(1);
        } else {
            final Path workingDirectory = Paths.get(args[0]).toAbsolutePath();
            DirectoryFluxkompensator directoryFluxkompensator = new DirectoryFluxkompensator();
            directoryFluxkompensator.process(workingDirectory);
        }
    }

    private void transform(Path dataXML, Path inputXSL, Path output) throws TransformerException {
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        TransformerFactory factory = TransformerFactory.newInstance();
        StreamSource xslStream = new StreamSource(inputXSL.toFile());
        Transformer transformer = factory.newTransformer(xslStream);
        StreamSource in = new StreamSource(dataXML.toFile());
        StreamResult out = new StreamResult(output.toFile());
        transformer.transform(in, out);
    }

    public void addLoggerCallback(LoggerCallback loggerCallback) {
        loggerCallbackList.add(loggerCallback);
    }

    private void log(String message) {
        for (LoggerCallback loggerCallback : loggerCallbackList) {
            loggerCallback.log(message);
        }
    }

    public void process(Path xmlPath, Path xslPath, Path outputPath) throws IOException {
        final Set<Path> xslFiles = new TreeSet<>();
        Files.walkFileTree(xslPath, new TreeSet<FileVisitOption>(), 1, new XslFileVisitor(xslFiles));
        outputPath.toFile().mkdirs();
        Files.walkFileTree(xmlPath, new TreeSet<FileVisitOption>(), 1, new XmlFileVisitor(xslFiles, outputPath));
    }

    public void process(Path workingDirectory) throws IOException {
        final Path xslPath = workingDirectory.resolve("xsl");
        final Path xmlPath = workingDirectory.resolve("xml");
        final Path outputPath = workingDirectory.resolve("output").toAbsolutePath();
        process(xmlPath, xslPath, outputPath);
    }

    private class XslFileVisitor implements FileVisitor<Path> {

        private Set<Path> xslFiles;

        private XslFileVisitor(Set<Path> xslFiles) {
            this.xslFiles = xslFiles;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.toString().endsWith(".xsl")) {
                xslFiles.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }

    private class XmlFileVisitor implements FileVisitor<Path> {

        private Set<Path> xslFiles;

        private Path outputPath;

        private XmlFileVisitor(Set<Path> xslFiles, Path outputPath) {
            this.xslFiles = xslFiles;
            this.outputPath = outputPath;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.toString().endsWith(".xml")) {
                for (Path xsl : xslFiles) {
                    log(String.format("Processing XML file '%s' with stylesheet '%s'", file.getFileName(), xsl.getFileName()));
                    try {
                        String xmlFilename = file.getFileName().toString().replaceAll(".xml", "");
                        String xslFilename = xsl.getName(xsl.getNameCount() - 1).toString().replaceAll(".xsl", "");
                        String csvFileName = String.format("%s-%s.csv", xmlFilename, xslFilename);
                        Path output = outputPath.resolve(csvFileName);
                        transform(file, xsl, output);
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    }
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

    }

}
