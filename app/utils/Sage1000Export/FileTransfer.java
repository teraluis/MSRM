package utils.Sage1000Export;

import com.jcraft.jsch.*;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import javax.inject.Inject;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileTransfer {
    protected final Logger logger = LoggerFactory.getLogger(FileTransfer.class);
    private final Config config;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private List<List<String>> parseFile(ChannelSftp sftp, String fileName, String prefix, String parseFolder) throws SftpException, IOException, CsvValidationException {
        List<List<String>> results = new ArrayList<>();
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        Reader targetReader = (new InputStreamReader(sftp.get("./" + parseFolder + "/" + fileName)));
        CSVReader csvReader = new CSVReaderBuilder(targetReader)
                .withSkipLines(1)
                .withCSVParser(parser)
                .build();
        String[] values;
        while ((values = csvReader.readNext()) != null) {
            results.add(Arrays.asList(values));
        }
        sftp.rename("./" + parseFolder + "/" + prefix + ".txt", "./" + parseFolder + "/" + String.format(prefix + "_%S.txt", dateFormat.format(new Date())));
        return results;
    }

    @Inject
    public FileTransfer(final Config config) {
        this.config = config;
    }

    public Boolean sendFile(final String fileName, final InputStream file, final Boolean export) {
        if (config.hasPath("sftp.server")) {
            final String host = config.getString("sftp.server.host");
            final Integer port = config.getInt("sftp.server.port");
            final String username = config.getString("sftp.server.username");
            final String password = config.getString("sftp.server.password");
            final String exportFolder = config.getString("sftp.server.exportfolder");
            Session session = null;
            try {
                JSch ssh = new JSch();
                session = ssh.getSession(username, host, port);
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword(password);
                session.connect();
                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftp = (ChannelSftp) channel;
                String path = export ? "./" + exportFolder + "/" + fileName : "./" + fileName;
                sftp.put(file, path);
                channel.disconnect();
                file.close();
                return true;
            } catch (JSchException e) {
                logger.error("Error during connection", e);
                return false;
            } catch (SftpException e) {
                logger.error("Error during file transfer", e);
                return false;
            } catch (IOException e) {
                logger.error("Error when closing stream", e);
                return false;
            } finally {
                if (session != null) {
                    session.disconnect();
                }
            }
        } else {
            logger.error("No config...");
            return false;
        }
    }

    public Optional<byte[]> getSingleFile(String filePath) {
        if (config.hasPath("sftp.server")) {
            final String host = config.getString("sftp.server.host");
            final Integer port = config.getInt("sftp.server.port");
            final String username = config.getString("sftp.server.username");
            final String password = config.getString("sftp.server.password");
            Session session = null;
            Channel channel = null;
            try {
                JSch ssh = new JSch();
                session = ssh.getSession(username, host, port);
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword(password);
                session.connect();
                session.setTimeout(30000);
                channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftp = (ChannelSftp) channel;
                InputStream inputStream = sftp.get("./" + filePath);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024 * 4];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                return Optional.of(buffer.toByteArray());
            } catch (JSchException e) {
                logger.error("Error during connection: ", e);
                return Optional.empty();
            } catch (IOException e) {
                logger.error("Error during parsing csv: ", e);
                return Optional.empty();
            } catch (SftpException e) {
                logger.error("Error during file transfer: ", e);
                return Optional.empty();
            } finally {
                if (channel != null) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }
        } else {
            logger.error("No config ...");
            return Optional.empty();
        }
    }

    public Optional<Tuple2<List<List<String>>, List<List<String>>>> getFiles() {
        if (config.hasPath("sftp.server")) {
            final String host = config.getString("sftp.server.host");
            final Integer port = config.getInt("sftp.server.port");
            final String username = config.getString("sftp.server.username");
            final String password = config.getString("sftp.server.password");
            final String parsingFolder = config.getString("sftp.server.parsingfolder");
            Session session = null;
            Channel channel = null;
            try {
                JSch ssh = new JSch();
                session = ssh.getSession(username, host, port);
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword(password);
                session.connect();
                session.setTimeout(30000);
                channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftp = (ChannelSftp) channel;
                Vector list = sftp.ls("./" + parsingFolder);
                List<List<String>> payments = new ArrayList<>();
                List<List<String>> irrecoverables = new ArrayList<>();
                for (Object aList : list) {
                    String fileName = ((ChannelSftp.LsEntry) aList).getFilename();
                    if (fileName.equals("SA1000_REGLEMENTS.txt")) {
                        payments = parseFile(sftp, fileName, "SA1000_REGLEMENTS", parsingFolder);
                    } else if (fileName.equals("SA1000_IRRECOUVRABLE.txt")) {
                        irrecoverables = parseFile(sftp, fileName, "SA1000_IRRECOUVRABLE", parsingFolder);
                    }
                }
                return Optional.of(new Tuple2<>(payments, irrecoverables));
            } catch (JSchException e) {
                logger.error("Error during connection: ", e);
                return Optional.empty();
            } catch (IOException | CsvValidationException e) {
                logger.error("Error during parsing csv: ", e);
                return Optional.empty();
            } catch (SftpException e) {
                logger.error("Error during file transfer: ", e);
                return Optional.empty();
            } finally {
                if (channel != null) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }
        } else {
            logger.error("No config ...");
            return Optional.empty();
        }
    }

    public Optional<List<String>> listFiles() {
        if (config.hasPath("sftp.server")) {
            final String host = config.getString("sftp.server.host");
            final Integer port = config.getInt("sftp.server.port");
            final String username = config.getString("sftp.server.username");
            final String password = config.getString("sftp.server.password");
            final String exportFolder = config.getString("sftp.server.exportfolder");
            final String parsingFolder = config.getString("sftp.server.parsingfolder");
            Session session = null;
            try {
                JSch ssh = new JSch();
                session = ssh.getSession(username, host, port);
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword(password);
                session.connect();
                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftp = (ChannelSftp) channel;
                Vector list = sftp.ls("./" + exportFolder);
                List<String> files = new ArrayList<>();
                for (Object aList : list) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) aList;
                    files.add(exportFolder + "/" + entry.getFilename());
                }
                Vector list2 = sftp.ls("./" + parsingFolder);
                for (Object aList : list2) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) aList;
                    files.add(parsingFolder + "/" + entry.getFilename());
                }
                channel.disconnect();
                return Optional.of(files);
            } catch (JSchException e) {
                logger.error("Error during connection: ", e);
                return Optional.empty();
            } catch (SftpException e) {
                logger.error("Error during file transfer: ", e);
                return Optional.empty();
            } finally {
                if (session != null) {
                    session.disconnect();
                }
            }
        } else {
            logger.error("No config ...");
            return Optional.empty();
        }
    }

    public Boolean renameFile(String oldFilePath, String newFilePath) {
        if (config.hasPath("sftp.server")) {
            final String host = config.getString("sftp.server.host");
            final Integer port = config.getInt("sftp.server.port");
            final String username = config.getString("sftp.server.username");
            final String password = config.getString("sftp.server.password");
            Session session = null;
            Channel channel = null;
            try {
                JSch ssh = new JSch();
                session = ssh.getSession(username, host, port);
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword(password);
                session.connect();
                session.setTimeout(30000);
                channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftp = (ChannelSftp) channel;
                sftp.rename("./" + oldFilePath, "./" + newFilePath);
                return true;
            } catch (JSchException e) {
                logger.error("Error during connection: ", e);
                return false;
            } catch (SftpException e) {
                logger.error("Error during file transfer: ", e);
                return false;
            } finally {
                if (channel != null) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }
        } else {
            logger.error("No config ...");
            return false;
        }
    }
}
