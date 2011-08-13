import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class Jeweled extends JApplet
{
  private static final long serialVersionUID = 1L;
  private JPanel canvas;
  private static final int APP_SIZE = 480;
  private static int MATRIX_SIZE = 8;
  private static int CELL_SIZE;
  private static int ITEM_NUM = 7;
  private static boolean REPLENISH = true;
  private static int TOUCH_WEIGHT = 30;
  private static final Random RND = new Random(System.currentTimeMillis());
  private volatile Cell[][] matrix;
  private volatile CellManager cellManager = new CellManager();
  private volatile boolean gameOver = false;
  private boolean loading = false;
  private Timer animator;

  public void init()
  {
    try
    {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          Jeweled.this.setSize(480, 480);
          Jeweled.this.canvas = new JPanel();
          Jeweled.this.canvas.setBackground(Color.WHITE);
          Jeweled.this.canvas.setDoubleBuffered(true);
          Jeweled.this.setContentPane(Jeweled.this.canvas);
          Jeweled.this.createGame();
        } } );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Set<Cell> checkMatrix() {
    Set<Cell> toDestroy = new HashSet<Cell>();
    for (int row = 0; row < MATRIX_SIZE; row++) {
      List<Cell> rowLine = new ArrayList<Cell>();
      List<Cell> colLine = new ArrayList<Cell>();
      for (int col = 0; col < MATRIX_SIZE; col++) {
        Cell currByRow = this.matrix[row][col];
        Cell currByCol = this.matrix[col][row];
        rowLine = checkItem(toDestroy, rowLine, currByRow);
        colLine = checkItem(toDestroy, colLine, currByCol);
      }
      if (rowLine.size() > 2) {
        toDestroy.addAll(rowLine);
        rowLine = new ArrayList<Cell>();
      }
      if (colLine.size() > 2) {
        toDestroy.addAll(colLine);
        colLine = new ArrayList<Cell>();
      }
    }
    return toDestroy;
  }

  public Cell getHint() {
    for (int i = 0; i < MATRIX_SIZE; i++) {
      for (int j = 0; j < MATRIX_SIZE; j++) {
        Cell start = this.matrix[i][j];
        Cell to = isHint(start);
        if (to != null) {
          System.out.println("Hint target: " + to.coordX + ":" + to.coordY + " : " + to.item.visual);
          System.out.println("Hint: " + start.coordX + ":" + start.coordY + " : " + start.item.visual);
          return start;
        }
      }
    }
    return null;
  }

  private Cell isHint(Cell cell) {
    if (cell.item == null) {
      return null;
    }
    int x = cell.coordX;
    int y = cell.coordY;
    if ((x > 0) && (!cell.item.equals(this.matrix[(x - 1)][y].item))) {
      if ((x - 3 >= 0) && (formsLine(cell.item, x - 3, y, x - 2, y))) return this.matrix[(x - 1)][y];
      if ((y - 2 >= 0) && (formsLine(cell.item, x - 1, y - 1, x - 1, y - 2))) return this.matrix[(x - 1)][y];
      if ((y - 1 >= 0) && (y + 1 < MATRIX_SIZE) && (formsLine(cell.item, x - 1, y - 1, x - 1, y + 1))) return this.matrix[(x - 1)][y];
      if ((y + 2 < MATRIX_SIZE) && (formsLine(cell.item, x - 1, y + 1, x - 1, y + 2))) return this.matrix[(x - 1)][y];
    }
    if ((y > 0) && (!cell.item.equals(this.matrix[x][(y - 1)].item))) {
      if ((y - 3 >= 0) && (formsLine(cell.item, x, y - 3, x, y - 2))) return this.matrix[x][(y - 1)];
      if ((x - 2 >= 0) && (formsLine(cell.item, x - 2, y - 1, x - 1, y - 1))) return this.matrix[x][(y - 1)];
      if ((x - 1 >= 0) && (x + 1 < MATRIX_SIZE) && (formsLine(cell.item, x - 1, y - 1, x + 1, y - 1))) return this.matrix[x][(y - 1)];
      if ((x + 2 < MATRIX_SIZE) && (formsLine(cell.item, x + 1, y - 1, x + 2, y - 1))) return this.matrix[x][(y - 1)];
    }
    if ((x + 1 < MATRIX_SIZE) && (!cell.item.equals(this.matrix[(x + 1)][y].item))) {
      if ((x + 3 < MATRIX_SIZE) && (formsLine(cell.item, x + 3, y, x + 2, y))) return this.matrix[(x + 1)][y];
      if ((y - 2 >= 0) && (formsLine(cell.item, x + 1, y - 2, x + 1, y - 1))) return this.matrix[(x + 1)][y];
      if ((y - 1 >= 0) && (y + 1 < MATRIX_SIZE) && (formsLine(cell.item, x + 1, y - 1, x + 1, y + 1))) return this.matrix[(x + 1)][y];
      if ((y + 2 < MATRIX_SIZE) && (formsLine(cell.item, x + 1, y + 2, x + 1, y + 1))) return this.matrix[(x + 1)][y];
    }
    if ((y + 1 < MATRIX_SIZE) && (!cell.item.equals(this.matrix[x][(y + 1)].item))) {
      if ((y + 3 < MATRIX_SIZE) && (formsLine(cell.item, x, y + 3, x, y + 2))) return this.matrix[x][(y + 1)];
      if ((x - 2 >= 0) && (formsLine(cell.item, x - 2, y + 1, x - 1, y + 1))) return this.matrix[x][(y + 1)];
      if ((x - 1 >= 0) && (x + 1 < MATRIX_SIZE) && (formsLine(cell.item, x - 1, y + 1, x + 1, y + 1))) return this.matrix[x][(y + 1)];
      if ((x + 2 < MATRIX_SIZE) && (formsLine(cell.item, x + 1, y + 1, x + 2, y + 1))) return this.matrix[x][(y + 1)];
    }
    return null;
  }

  private boolean formsLine(Item item, int x1, int y1, int x2, int y2)
  {
    return (item.equals(this.matrix[x1][y1].item)) &&
      (item.equals(this.matrix[x2][y2].item));
  }

  private List<Cell> checkItem(Set<Cell> toDestroy, List<Cell> line, Cell curr)
  {
    if (!line.contains(curr)) {
      if (line.size() == 0) {
        line.add(curr);
      } else if (((line.get(0)).item != null) && (curr.item != null) &&
        ((line.get(0)).item.equals(curr.item))) {
        line.add(curr);
      } else {
        if (line.size() > 2) {
          toDestroy.addAll(line);
        }
        line = new ArrayList<Cell>();
        line.add(curr);
      }
    }
    return line;
  }

  private void destroyCells(Set<Cell> toDestroy) {
    if (toDestroy.size() > 0) {
      for (Cell c : toDestroy) {
        c.item = null;
        if (!this.loading) c.destroyed = (TOUCH_WEIGHT * 3);
        c.repaint();
      }
    }

    boolean fallen = true;
    while (fallen) {
      fallen = false;
      for (int i = MATRIX_SIZE - 1; i >= 0; i--) {
        for (int j = MATRIX_SIZE - 1; j >= 0; j--) {
          if ((this.matrix[i][j].item != null) ||
            (i <= 0) ||
            (this.matrix[(i - 1)][j].item == null)) continue;
          if (this.matrix[i][j] == null) {
            System.out.println("NULL!");
          }
          this.cellManager.swap(this.matrix[i][j], this.matrix[(i - 1)][j]);
          fallen = true;
        }

      }

    }

    if (REPLENISH)
    {
      for (int i = 0; i < MATRIX_SIZE; i++) {
        for (int j = 0; j < MATRIX_SIZE; j++) {
          if (this.matrix[i][j].item == null) {
            List<Item> forbidden = new ArrayList<Item>();
            if (i > 0) {
              forbidden.add(this.matrix[(i - 1)][j].item);
            }
            if (j < MATRIX_SIZE - 1) {
              forbidden.add(this.matrix[i][(j + 1)].item);
            }
            if (j > 0) {
              forbidden.add(this.matrix[i][(j - 1)].item);
            }
            this.matrix[i][j].item = Item.getRandom(forbidden);
            if (this.loading) continue; this.matrix[i][j].created = (TOUCH_WEIGHT * 3);
          }
        }
      }
    }
    Cell hint = getHint();
    if (hint == null) {
      this.gameOver = true;
      System.out.println("Game over");
      repaint();
    }
  }

  public void start()
  {
    this.animator = new Timer(10, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if ((Jeweled.this.gameOver) || (Jeweled.this.loading)) return;
        synchronized (Jeweled.this.matrix) {
          for (Jeweled.Cell[] cells : Jeweled.this.matrix)
            for (Jeweled.Cell cell : cells) {
                if (cell.touched > 0) {
                    cell.touched -= 1;
                }
                if (cell.created > 0) {
                    cell.created -= 1;
                }
                if (cell.destroyed > 0) {
                    cell.destroyed -= 1;
                }
                if (cell.hover > 0) {
                    cell.hover -= 1;
                }
                cell.repaint();
            }
        }
      }
    });
    this.animator.setCoalesce(false);
    this.animator.setRepeats(true);
    this.animator.start();
  }

  public void stop()
  {
    this.animator.stop();
  }

  private synchronized void createGame() {
    this.loading = true;
    this.gameOver = false;
    try {
      MATRIX_SIZE = Integer.parseInt(JOptionPane.showInputDialog(this, "Number of cells in a row (3-20)", 8));
      ITEM_NUM = Integer.parseInt(JOptionPane.showInputDialog(this, "Number of different items (4-9)", 7));
      REPLENISH = JOptionPane.showConfirmDialog(this, "Regenerate destroyed cells?", "Choose", 0) == 0;
    } catch (Exception e) {
      System.out.println("Bad user input...");
    }
    if (MATRIX_SIZE < 3) MATRIX_SIZE = 3;
    if (MATRIX_SIZE > 20) MATRIX_SIZE = 20;
    CELL_SIZE = 480 / MATRIX_SIZE;
    if (ITEM_NUM < 4) ITEM_NUM = 4;
    if (ITEM_NUM > 9) ITEM_NUM = 9;
    if (this.matrix != null) {
      this.canvas.removeAll();
    }
    this.matrix = new Cell[MATRIX_SIZE][MATRIX_SIZE];
    GridLayout layout = new GridLayout(0, MATRIX_SIZE, 0, 0);
    this.canvas.setLayout(layout);
    for (int i = 0; i < MATRIX_SIZE; i++) {
      for (int j = 0; j < MATRIX_SIZE; j++) {
        Cell cell = new Cell();
        cell.item = Item.getRandom();
        cell.setCoordinates(i, j);
        this.matrix[i][j] = cell;
        this.canvas.add(cell);
      }
    }
    boolean regen = REPLENISH;
    REPLENISH = true;
    Set<Cell> toDestroy = checkMatrix();
    while (toDestroy.size() > 0) {
      destroyCells(toDestroy);
      toDestroy = checkMatrix();
      System.out.println("Destroyed cells");
    }
    REPLENISH = regen;
    Cell hint = getHint();
    if (hint == null) {
      this.gameOver = true;
    }
    this.loading = false;
    this.canvas.doLayout();
    repaint();
  }

  public void paint(Graphics g)
  {
    super.paint(g);
    if (this.gameOver) {
      ((Graphics2D)g).clearRect(0, 0, getWidth(), getHeight());
      Font f = g.getFont();
      Font ff = f.deriveFont(1, 50.0F);
      Color c = g.getColor();
      g.setColor(Color.RED);
      g.setFont(ff);
      g.drawString("GAME OVER", 100, 250);
      g.setColor(c);
      g.setFont(ff);
    }
  }

  private class Cell extends JComponent
  {
    private volatile Jeweled.Item item;
    private volatile int hover = 0;
    private volatile int destroyed = 0;
    private volatile int created = 0;
    private boolean hovered = false;
    private boolean active = false;
    protected int coordX;
    protected int coordY;
    protected volatile int touched = 0;
    private static final long serialVersionUID = 1L;

    public void setActive(boolean active)
    {
      this.active = active;
    }

    public Cell() {
      //Cell self = this;
      setSize(Jeweled.CELL_SIZE, Jeweled.CELL_SIZE);
      addMouseListener(new MouseAdapter()
      {
        public void mouseEntered(MouseEvent e) {
          Jeweled.Cell.this.hover = Math.min(Jeweled.Cell.this.hover + 50, 255);
          Jeweled.Cell.this.hovered = true;
          Jeweled.Cell.this.repaint();
        }

        public void mouseExited(MouseEvent e)
        {
          Jeweled.Cell.this.hovered = false;
          Jeweled.Cell.this.repaint();
        }

        public void mouseClicked(MouseEvent e)
        {
          if (Jeweled.this.gameOver) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                Jeweled.this.createGame();
              }
            });
            return;
          }
          Jeweled.Cell.this.active = Jeweled.this.cellManager.clicked(Cell.this);
          Jeweled.Cell.this.repaint();
        }
      });
    }

    public void paint(Graphics g)
    {
      if (Jeweled.this.gameOver) {
        g.setColor(Color.RED);
        g.fillRect(0, 0, Jeweled.CELL_SIZE - 1, Jeweled.CELL_SIZE - 1);
        return;
      }
      Graphics2D g2 = (Graphics2D)g;
      Color c = g.getColor();
      Color bg = g2.getBackground();
      if (this.active) { g2.setBackground(Color.MAGENTA);
      } else if (this.hovered) { g2.setBackground(new Color(200, 200, 255));
      } else {
        int backRed = 255; int backGreen = 255; int backBlue = 255;
        backRed -= this.hover; backGreen -= this.hover;
        backRed -= this.touched; backBlue -= this.touched;
        backGreen -= this.destroyed; backBlue -= this.destroyed;
        backGreen -= this.created;
        g2.setBackground(new Color(Math.max(0, backRed), Math.max(0, backGreen), Math.max(0, backBlue)));
      }
      g2.clearRect(0, 0, Jeweled.CELL_SIZE - 1, Jeweled.CELL_SIZE - 1);
      g.setColor(Color.BLACK);
      g.drawRect(0, 0, Jeweled.CELL_SIZE - 1, Jeweled.CELL_SIZE - 1);
      if (this.item != null) this.item.paint(g);
      g.setColor(c);
      if ((this.hovered) || (this.active))
        g2.setBackground(bg);
    }

    public void setCoordinates(int i, int j)
    {
      this.coordX = i;
      this.coordY = j;
    }
  }

  private class CellManager
  {
    private Jeweled.Cell chosen;

    private CellManager()
    {
    }

    public boolean clicked(Jeweled.Cell cell)
    {
      if (Jeweled.this.gameOver) return false;
      Jeweled.RND.setSeed(System.currentTimeMillis());
      boolean activate = false;
      if (this.chosen != null) {
        this.chosen.setActive(false);
        this.chosen.repaint();
        if (isNeighbour(cell, this.chosen)) {
          swap(this.chosen, cell);
          Set<Cell> toDestroy = Jeweled.this.checkMatrix();
          if (toDestroy.size() > 0)
            while (toDestroy.size() > 0) {
              Jeweled.this.destroyCells(toDestroy);
              toDestroy = Jeweled.this.checkMatrix();
            }
          else {
            swap(this.chosen, cell);
          }
        }
        this.chosen = null;
      } else {
        this.chosen = cell;
        activate = true;
      }
      return activate;
    }

    private boolean isNeighbour(Jeweled.Cell one, Jeweled.Cell two)
    {
      return ((one.coordX == two.coordX + 1) || (one.coordX == two.coordX - 1)) && (
        (one.coordY == two.coordY) || (
        ((one.coordY == two.coordY + 1) || (one.coordY == two.coordY - 1)) &&
        (one.coordX == two.coordX)));
    }

    private void swap(Jeweled.Cell one, Jeweled.Cell two) {
      System.out.println("Swapping: " + one.item + " with " + two.item);
      Jeweled.Item temp = one.item;
      one.item = two.item;
      two.item = temp;
      if (!Jeweled.this.loading) {
        one.touched = Math.min(one.touched + Jeweled.TOUCH_WEIGHT, 255);
        two.touched = Math.min(two.touched + Jeweled.TOUCH_WEIGHT, 255);
      }
      temp = null;
      one.repaint();
      two.repaint();
    }
  }

  private static enum Item
  {
    ITEM_1("✖", Color.RED),
    ITEM_2("✤", new Color(50, 200, 20)),
    ITEM_3("✸", Color.BLUE),
    ITEM_4("❤", new Color(200, 50, 20)),
    ITEM_5("❅", Color.ORANGE),
    ITEM_6("✠", new Color(20, 100, 100)),
    ITEM_7("❖", Color.BLACK),
    ITEM_8("✪", new Color(100, 20, 100)),
    ITEM_9("✾", new Color(20, 50, 200));

    private final String visual;
    private final Color color;

    private Item(String visual, Color color) { this.visual = visual;
      this.color = color; }

    public void paint(Graphics g)
    {
      Color c = g.getColor();
      g.setColor(this.color);
      Font f = g.getFont();
      Font ff = f.deriveFont(1, Jeweled.CELL_SIZE * 2 / 3);
      g.setFont(ff);
      g.drawString(this.visual, Jeweled.CELL_SIZE / 4, Jeweled.CELL_SIZE - Jeweled.CELL_SIZE / 4);
      g.setFont(f);
      g.setColor(c);
    }

    public static Item getRandom() {
      return values()[Jeweled.RND.nextInt(Jeweled.ITEM_NUM)];
    }

    public static Item getRandom(List<Item> forbidden) {
      Item rand = getRandom();
      if (forbidden != null) {
        while (forbidden.contains(rand)) rand = getRandom();
      }
      System.out.println("Falling from the sky: " + rand.visual);
      return rand;
    }
  }
}
