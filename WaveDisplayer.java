import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class WaveDisplayer extends JFrame {
    private FileInputStream fis;
    private BufferedInputStream bis;
    private int[] samples;
    private int maxValue;
    private int sampleCount;

    public int twoBytesToInt(byte[] bytes, int offset) {
        return ((int)(bytes[offset]) << 8) | (bytes[offset - 1] & 0xFF);
    }

    public int fourBytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset - 1] & 0xFF) << 16) |
                ((bytes[offset - 2] & 0xFF) << 8) | (bytes[offset - 3] & 0xFF);
    }

    public WaveDisplayer(String fileName) throws IOException {
        fis = new FileInputStream(fileName);
        bis = new BufferedInputStream(fis);

        byte[] header = new byte[44];
        bis.read(header, 0, 44);

        int sampleSize = twoBytesToInt(header, 35);
        int dataSize = fourBytesToInt(header, 43);
        sampleCount = (int)(dataSize / (sampleSize / 8));

        samples = new int[sampleCount];

        if(sampleSize == 8) {
            for(int i = 0; i < sampleCount; i++) {
                samples[i] = bis.read();
            }
        } else {
            byte[] temp = new byte[2];
            for(int i = 0; i < sampleCount; i++) {
                bis.read(temp);
                samples[i] = twoBytesToInt(temp, 1);
            }
        }

        findMaxValue();

        fis.close();
        bis.close();
    }

    public void findMaxValue() {
        maxValue = Integer.MIN_VALUE;

        for(int elem : samples) {
            maxValue = Integer.max(maxValue, elem);
        }
    }

    public void showWave(int width, int height) {
        this.setTitle("Wave Displayer");
        this.setSize(width, height);
        this.setDefaultCloseOperation(3);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        System.out.println("Maximum value: " + maxValue);
        System.out.println("The number of samples: " + sampleCount);

        Waveform waveform = new Waveform();
        Dimension dimension = new Dimension(width, height);
        waveform.setPreferredSize(dimension);
        this.add(waveform);
        this.setVisible(true);
    }

    public class Waveform extends JPanel {
        public void paint(Graphics g) {
            super.paint(g);
            int width = getWidth();
            int height = getHeight();
            int step = sampleCount / width;
            double intervalY = height / 2.0 / maxValue / 1.5;
            int preX, preY, curX, curY;

            preX = preY = 0;
            g.setColor(new Color(30, 100, 50));

            for(int i = 0; i < width; i++) {
                curX = i;
                curY = height - (int)(samples[i * step] * intervalY + height / 2);

                g.drawLine(preX, preY, curX, curY);

                preX = curX;
                preY = curY;
            }
        }
    }

    public static void main(String[] args) {
        try {
            FileSelector fs = new FileSelector();
            if(fs.getPath() != null && fs.getPath().endsWith(".wav")) {
                WaveDisplayer wd = new WaveDisplayer(fs.getPath());
                wd.showWave(1900, 400);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
