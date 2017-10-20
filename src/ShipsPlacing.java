import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

class ShipsPlacing extends JLayeredPane{

    private ShipsPlacing me;
    private PlayerBoard playerBoard;
    private GraphicalBoard graphicalBoard;
    private Client _client;

    ShipsPlacing(Client client){
        _client = client;

        setLayout(null);
        playerBoard = new PlayerBoard();
        me = this;
        setLocation(0,0);
        setSize(Client.DIMENSION);
        setBackground(Color.WHITE);

        graphicalBoard = new GraphicalBoard(playerBoard);

        addMouseListener(new SpecialMouseListener());
        addMouseMotionListener(new SpecialMouseListener());

        for(GraphShip graphShip : GraphShip.getAll()) {
            add(graphShip, 1, 5);
        }

        add(graphicalBoard, 0, 7);
        graphicalBoard.lightItForNow();

    }

    private class SpecialMouseListener extends MouseAdapter {

        GraphShip currentFocused;

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if(SwingUtilities.isRightMouseButton(e)){
                currentFocused.rotate();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            try{
                currentFocused = (GraphShip) me.findComponentAt(e.getPoint());
                currentFocused.setBackground(Color.BLACK);
                System.out.println(currentFocused);
                if(currentFocused.alreadyPlaced){
                    playerBoard.removeShip(currentFocused.getShip());
                    remove(graphicalBoard);
                    graphicalBoard = new GraphicalBoard(playerBoard);
                    graphicalBoard.lightItForNow();
                    add(graphicalBoard, 0,7);
                    me.repaint();
                }
            }catch (ClassCastException exc){
                currentFocused = null;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            //System.out.println(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(currentFocused != null){
                currentFocused.setLocation(e.getPoint());
                Point coordinatesFromClick = Client.getCoordinatesFromClick(e.getPoint());
                if(coordinatesFromClick != null) {
                    currentFocused.changeShipPosition(coordinatesFromClick);
                    if(playerBoard.canShipBeHere(currentFocused.getShip())){
                        playerBoard.placeShip(currentFocused.getShip());
                        currentFocused.alreadyPlaced = true;
                        currentFocused.setBackground(new Color(1f,0f,0f,0.0f));
                        remove(graphicalBoard);
                        graphicalBoard = new GraphicalBoard(playerBoard);
                        graphicalBoard.lightItForNow();
                        add(graphicalBoard, 0,7);
                        me.repaint();
                        if(playerBoard.fullOfShips()){
                            _client.shipsSet = true;
                        }
                    }
                }
                else{
                    currentFocused.alreadyPlaced = false;
                }
            }
            //System.out.println(e);
        }

        private boolean insideBorders(){
            return true;
        }
    }
}