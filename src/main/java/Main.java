import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.*;

public class Main extends JFrame {

    public static void main(String[] args) {
        new Main("ColorCount");
    }

    private JPanel content;

    public Main(String title) {
        super(title);

        this.setLayout(new FlowLayout());

        String img = Main.class.getResource("drop-files-here-extra.jpg").toString();
        JEditorPane dropPane = new JEditorPane("text/html", "<html><p style=\"text-align: center\"><img src='" + img + "'height=150></img></p>");
        dropPane.setEditable(false);
        dropPane.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles = (java.util.List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    BufferedImage icon = ImageIO.read(droppedFiles.get(0));
                    droppedImage(icon);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        this.add(dropPane);


        content = new JPanel();
        JScrollPane scrollPane = new JScrollPane(content);
        content.setLayout(new GridLayout(0, 1));
        scrollPane.setPreferredSize(new Dimension(300, 400));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane);

        this.setSize(320, 625);
        setLocationRelativeTo(null);
        this.setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void droppedImage(BufferedImage image) {
        if (image != null) {
            Map<String, Integer> colorCount = countColors(image);

            Comparator<Map.Entry<String, Integer>> valueComparator = Map.Entry.comparingByValue();
            java.util.List<Map.Entry<String, Integer>> listOfEntries = new ArrayList<>(colorCount.entrySet());
            listOfEntries.sort(valueComparator);
            int totalPackCount = 0;
            content.removeAll();
            for (Map.Entry<String, Integer> entry : listOfEntries) {
                Color color = stringToColor(entry.getKey());
                int packCount = (int) Math.ceil(entry.getValue() / 500.0);
                totalPackCount += packCount;
                JLabel label = new JLabel(entry.getKey() + ": " + entry.getValue() + " (" + packCount + ")");
                label.setOpaque(true);
                label.setBackground(color);
                label.setForeground(getForeground(color));
                content.add(label);
            }
            JLabel total = new JLabel("Total: " + totalPackCount + " (~" + (totalPackCount * 3) + "€)");
            content.add(total);
            this.revalidate();
            this.repaint();
        }
    }

    private Color stringToColor(String string) {
        return new Color(Integer.parseInt(string.substring(0, 2), 16),
                Integer.parseInt(string.substring(2, 4), 16),
                Integer.parseInt(string.substring(4, 6), 16));
    }

    private Map<String, Integer> countColors(BufferedImage image) {
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        HashMap<String, Integer> map = new HashMap<>();

        final int pixelLength = hasAlphaChannel ? 4 : 3;
        for (int pixel = 0; pixel + (pixelLength - 1) < pixels.length; pixel += pixelLength) {
            String hex = String.format("%02X%02X%02X", pixels[pixel + 3], pixels[pixel + 2], pixels[pixel + 1]);
            int count = map.getOrDefault(hex, 0);
            map.put(hex, count + 1);
        }
        return map;
    }

    private Color getForeground(Color background) {
        int brightness = background.getRed() + background.getGreen() + background.getBlue();
        if (brightness > 400)
            return Color.black;
        return Color.white;
    }
}
