package eu.artofcoding.kellerautomat.xml2csv;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.TreeSet;

public class DirectoryFluxkompensator {

    public static void transform(Path dataXML, Path inputXSL, Path output) throws TransformerConfigurationException, TransformerException {
        net.sf.saxon.TransformerFactoryImpl t;
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        TransformerFactory factory = TransformerFactory.newInstance();
        StreamSource xslStream = new StreamSource(inputXSL.toFile());
        Transformer transformer = factory.newTransformer(xslStream);
        StreamSource in = new StreamSource(dataXML.toFile());
        StreamResult out = new StreamResult(output.toFile());
        transformer.transform(in, out);
    }

    public static void main(String[] args) throws IOException {
        final Path workingDirectory = Paths.get(args[0]).toAbsolutePath();
        final Path xslPath = workingDirectory.resolve("xsl");
        final Set<Path> xslFiles = new TreeSet<>();
        Files.walkFileTree(xslPath, new TreeSet<FileVisitOption>(), 1, new XslFileVisitor(xslFiles));
        final Path xmlPath = workingDirectory.resolve("xml");
        final Path outputPath = workingDirectory.resolve("output").toAbsolutePath();
        outputPath.toFile().mkdirs();
        Files.walkFileTree(xmlPath, new TreeSet<FileVisitOption>(), 1, new XmlFileVisitor(xslFiles, outputPath));
    }

    private static class XmlFileVisitor implements FileVisitor<Path> {

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
            for (Path xsl : xslFiles) {
                System.out.printf("Processing XML file '%s' with stylesheet '%s'%n", file.getFileName(), xsl.getFileName());
                try {
                    String xmlFilename = file.getFileName().toString().replaceAll(".xml", "");
                    String xslFilename = xsl.getName(xsl.getNameCount()-1).toString().replaceAll(".xsl", "");
                    Path output = outputPath.resolve(xmlFilename + "-" + xslFilename + ".csv");
                    transform(file, xsl, output);
                } catch (TransformerException e) {
                    e.printStackTrace();
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

    private static class XslFileVisitor implements FileVisitor<Path> {

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
            xslFiles.add(file);
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
