package dominio;

import dominio.repositorio.RepositorioProducto;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.NoResultException;

import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioGarantiaExtendida;

public class Vendedor {

    public static final String EL_PRODUCTO_TIENE_GARANTIA = "El producto ya cuenta con una garantía extendida";
    public static final String ESTE_PRODUCTo_HO_CUENTA_CON_GARANTIA_EXTENDIDA = "Este producto no cuenta con garantía extendida";
    public static final String NO_SE_ENCUETRA_UN_PRODUCTO_CON_ESTE_CODIGO = "No se encuentra un producto con este código";
    private static final int CANTIDAD_VOCALES = 3;
    private static final double PRECIO_PRODUCTO = 500000;
    private static final double VEINTE_PORCIENTO = 0.2;
    private static final double DIEZ_PORCIENTO = 0.1;
    private static final int DOSCIENTOS_DIAS_DE_GARANTIA = 200;
    private static final int CIEN_DIAS_DE_GARANTIA = 100;
    private static final boolean INCLUIR_FECHA_SOLICITUD_GARANTIA = true;

    private RepositorioProducto repositorioProducto;
    private RepositorioGarantiaExtendida repositorioGarantia;
    private Date fechaSolicitudGarantia;

    public Vendedor(RepositorioProducto repositorioProducto, RepositorioGarantiaExtendida repositorioGarantia, Date fechaSolicitudGarantia) {
        this.repositorioProducto = repositorioProducto;
        this.repositorioGarantia = repositorioGarantia;
        this.fechaSolicitudGarantia = fechaSolicitudGarantia;
    }

    public void generarGarantia(String codigo, String nombreCliente) {    	
    	try{
    		if(tieneGarantia(codigo)){
        		throw new GarantiaExtendidaException(EL_PRODUCTO_TIENE_GARANTIA);
        	}else if(tieneXVocales(codigo)){
        		throw new GarantiaExtendidaException(ESTE_PRODUCTo_HO_CUENTA_CON_GARANTIA_EXTENDIDA);
        	}else if(precioMayorA(repositorioProducto.obtenerPorCodigo(codigo).getPrecio())){    		
        		double precioGarantia = repositorioProducto.obtenerPorCodigo(codigo).getPrecio() * VEINTE_PORCIENTO;    	
        		Date fechaFinGarantia = calcularFechaFinGarantia(DOSCIENTOS_DIAS_DE_GARANTIA);    		
        		repositorioGarantia.agregar(new GarantiaExtendida(repositorioProducto.obtenerPorCodigo(codigo), fechaSolicitudGarantia, fechaFinGarantia, precioGarantia, nombreCliente));
        	}else{        		
        		double precioGarantia = repositorioProducto.obtenerPorCodigo(codigo).getPrecio() * DIEZ_PORCIENTO;  
        		Date fechaFinGarantia = calcularFechaFinGarantia(CIEN_DIAS_DE_GARANTIA);    		
        		repositorioGarantia.agregar(new GarantiaExtendida(repositorioProducto.obtenerPorCodigo(codigo), fechaSolicitudGarantia, fechaFinGarantia, precioGarantia, nombreCliente));
        	}
    	}catch (NoResultException nre){
    		throw new GarantiaExtendidaException(NO_SE_ENCUETRA_UN_PRODUCTO_CON_ESTE_CODIGO);
    	}
    }
    
    public boolean tieneXVocales(String codigo){
    	int cantidadVocales = 0;
    	String codigoSinMayusculas = codigo.toLowerCase();
    	int tamanioStringCodigo = codigo.length();    	
    	for (int i = 0; i < tamanioStringCodigo; i++) {
			char caracter = codigoSinMayusculas.charAt(i);
			if(caracter == 'a' || caracter == 'e' || caracter == 'i' || caracter == 'o' || caracter == 'u'){
				cantidadVocales++;
			}			
		}    	
    	if(cantidadVocales == CANTIDAD_VOCALES){
    		return true;
    	}else{
    		return false;
    	}    	
    }
    
    public boolean precioMayorA(double precio){
    	if(precio > PRECIO_PRODUCTO){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public Date calcularFechaFinGarantia(int diasExtensionGarantia){
    	diasExtensionGarantia = (INCLUIR_FECHA_SOLICITUD_GARANTIA) ? diasExtensionGarantia - 1 : diasExtensionGarantia;
    	Calendar calendario = Calendar.getInstance();
    	calendario.setTime(fechaSolicitudGarantia);
    	for (int i = 0; i < diasExtensionGarantia; i++) {
			calendario.add(Calendar.DATE, 1);
			//Saltarse los días lunes
			if(calendario.get(Calendar.DAY_OF_WEEK) == (Calendar.MONDAY)){
				 calendario.add(Calendar.DATE, 1);
			 }	
		}
    	//Si la fecha final de la garantía cae un domingo, entonces aumentar 2 días (ya que el lunes no se cuenta)
    	if(calendario.get(Calendar.DAY_OF_WEEK) == (Calendar.SUNDAY)){
			 calendario.add(Calendar.DATE, 2);
		 }
    	return calendario.getTime();
    }
    

    public boolean tieneGarantia(String codigo) {
        if(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(codigo)!=null) {
        	return true;
        }else {
        	return false;
        }
    }

}
