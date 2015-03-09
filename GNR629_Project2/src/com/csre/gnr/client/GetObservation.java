package com.csre.gnr.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

public class GetObservation {
    private List<Obs> obsList = new ArrayList<Obs>();
    Obs oFirst;
    
    // A simple data type that represents a contact.
    private static class Obs {
        private String f1;
        private String f2;
        private String f3;
        private String f4;
        private String f5;

        public Obs(String f1, String f2, String f3, String f4, String f5) {
            this.f1 = f1;
            this.f2 = f2;
            this.f3 = f3;
            this.f4 = f4;
            this.f5 = f5;
        }

        public void set(int i, String s) {
            switch(i){
                case 0:
                    this.f1 = s;
                    break;
                case 1:
                    this.f2 = s;
                    break;
                case 2:
                    this.f3 = s;
                    break;
                case 3:
                    this.f4 = s;
                    break;
                case 4:
                    this.f5 = s;
                    break;
                default:
                    this.f5 += ","+s;
            }
        }
        
        public String toString(){
            String t="{";
            t += f1 + ",";
            t += f2 + ",";
            t += f3 + ",";
            t += f4 + ",";
            t += f5 + "}";
            
            return t;
        }
    }

    public void getObs(String url) {
        String requestData = null;
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            builder.sendRequest(requestData, new RequestCallback() {
                
                public void onResponseReceived(Request request,
                        Response response) {
                    String csvDoc;
                    if (200 == response.getStatusCode()) {
                        csvDoc = response.getText();
                        Window.alert(csvDoc);
                        
                        parseCSV(csvDoc);
                        
                        // Create a CellTable.
                        CellTable<Obs> table = new CellTable<Obs>();

                        // Create f1 column.
                        TextColumn<Obs> f1Column = new TextColumn<Obs>() {
                            @Override
                            public String getValue(Obs obs) {
                                return obs.f1;
                            }
                        };

                        // Make the f1 column sortable.
                        f1Column.setSortable(true);

                        // Create f2 column.
                        TextColumn<Obs> f2Column = new TextColumn<Obs>() {
                            @Override
                            public String getValue(Obs obs) {
                                return obs.f2;
                            }
                        };

                        // Create f3 column.
                        TextColumn<Obs> f3Column = new TextColumn<Obs>() {
                            @Override
                            public String getValue(Obs obs) {
                                return obs.f3;
                            }
                        };
                        // Create f4 column.
                        TextColumn<Obs> f4Column = new TextColumn<Obs>() {
                            @Override
                            public String getValue(Obs obs) {
                                return obs.f4;
                            }
                        };

                        // Create f5 column.
                        TextColumn<Obs> f5Column = new TextColumn<Obs>() {
                            @Override
                            public String getValue(Obs obs) {
                                return obs.f5;
                            }
                        };

                        // Add the columns.
                        table.addColumn(f1Column, oFirst.f1);
                        table.addColumn(f2Column, oFirst.f2);
                        table.addColumn(f3Column, oFirst.f3);
                        table.addColumn(f4Column, oFirst.f4);
                        table.addColumn(f5Column, oFirst.f5);

                        // Create a data provider.
                        ListDataProvider<Obs> dataProvider = new ListDataProvider<Obs>();

                        // Connect the table to the data provider.
                        dataProvider.addDataDisplay(table);

                        // Add the data to the data provider, which automatically pushes it to
                        // the
                        // widget.
                        List<Obs> list = dataProvider.getList();
                        for (Obs o : obsList) {
                            list.add(o);
                        }

                        // Add a ColumnSortEvent.ListHandler to connect sorting to the
                        // java.util.List.
                        ListHandler<Obs> columnSortHandler = new ListHandler<Obs>(list);
                        columnSortHandler.setComparator(f1Column,new Comparator<Obs>() {
                            @Override
                            public int compare(Obs o1, Obs o2) {
                                if (o1 == o2) {
                                    return 0;
                                }

                                // Compare the name columns.
                                if (o1 != null) {
                                    return (o2 != null) ? o1.f1.compareTo(o2.f1) : 1;
                                }
                                return -1;
                            }
                        });
                        table.addColumnSortHandler(columnSortHandler);

                        // We know that the data is sorted alphabetically by default.
                        table.getColumnSortList().push(f1Column);

                        // Create a SimplePager.
                        SimplePager pager = new SimplePager();
                        // Set the table as the display.
                       pager.setDisplay(table);
                        // Add the pager and list to the page.
                        VerticalPanel vPanel = new VerticalPanel();  
                        vPanel.add(table);
                       vPanel.add(pager);
                        
                        // Add it to the root panel.
                        DialogBox db=new DialogBox(true,true);
                        db.setAnimationEnabled(true);
                        db.add(vPanel);
                        db.center();
                        obsList.clear();
                    }
                }

                private void parseCSV(String csv) {
                    String lines[] = csv.split("\n");
                    //Window.alert(lines.length+"");
                    for(String line : lines){
                        String element[] = line.split(",");
                        //Window.alert(element.length+"");
                        Obs otemp = new Obs("","","","","");
                        
                        for(int i=0; i<element.length; i++){
                            otemp.set(i,element[i]);
                        }
                        //Window.alert(otemp.toString());
                        obsList.add(otemp);
                    }
                    
                    if( !obsList.isEmpty() ) {
                        oFirst = obsList.get(0);
                        obsList.remove(0);
                    }
                    if(obsList.isEmpty()) {
                        obsList.add(new Obs("","","No Data Available","",""));
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Window.alert(exception.getMessage());
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    public void getObservations(String url) {
        getObs(url);
                
    }
}
