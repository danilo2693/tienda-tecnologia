package dominio.integracion;

import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dominio.Vendedor;
import dominio.Producto;
import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioProducto;
import dominio.repositorio.RepositorioGarantiaExtendida;
import persistencia.sistema.SistemaDePersistencia;
import testdatabuilder.ProductoTestDataBuilder;

public class VendedorTest {

	private static final String COMPUTADOR_LENOVO = "Computador Lenovo";
	private static final String DANILO_ROMAN = "Danilo Roman";	
	private static final double PRECIO_PRODUCTO_TEST_200_DIAS = 650000;
	private static final double PRECIO_GARANTIA_TEST_200_DIAS = 130000;
	
	private static final double PRECIO_PRODUCTO_TEST_100_DIAS = 400000;
	private static final double PRECIO_GARANTIA_TEST_100_DIAS = 40000;
	
	private static final double PRECIO_PRODUCTO_TEST_200_DIAS_CAYENDO_DOMINGO = 800000;
	private static final double PRECIO_GARANTIA_TEST_200_DIAS_CAYENDO_DOMINGO = 160000;
	
	private static final double PRECIO_PRODUCTO_TEST_100_DIAS_CAYENDO_DOMINGO = 250000;
	private static final double PRECIO_GARANTIA_TEST_100_DIAS_CAYENDO_DOMINGO = 25000;
	
	private static final String CODIGO_PRODUCTO_CON_TRES_VOCALES = "AEITS00150";
	
	private static final String CODIGO_PRODUCTO_ERRONEO = "A1I2450759";
	
	private SistemaDePersistencia sistemaPersistencia;
	
	private RepositorioProducto repositorioProducto;
	private RepositorioGarantiaExtendida repositorioGarantia;

	@Before
	public void setUp() {
		
		sistemaPersistencia = new SistemaDePersistencia();
		
		repositorioProducto = sistemaPersistencia.obtenerRepositorioProductos();
		repositorioGarantia = sistemaPersistencia.obtenerRepositorioGarantia();
		
		sistemaPersistencia.iniciar();
	}
	

	@After
	public void tearDown() {
		sistemaPersistencia.terminar();
	}

	@Test
	public void generarGarantiaTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia, new Date());

		// act
		vendedor.generarGarantia(producto.getCodigo(), DANILO_ROMAN);

		// assert
		Assert.assertTrue(vendedor.tieneGarantia(producto.getCodigo()));
		Assert.assertNotNull(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo()));
		Assert.assertEquals(DANILO_ROMAN, repositorioGarantia.obtener(producto.getCodigo()).getNombreCliente());

	}

	@Test
	public void productoYaTieneGarantiaTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();
		
		repositorioProducto.agregar(producto);
		
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia, new Date());

		// act
		vendedor.generarGarantia(producto.getCodigo(), DANILO_ROMAN);;
		try {
			
			vendedor.generarGarantia(producto.getCodigo(), DANILO_ROMAN);
			fail();
			
		} catch (GarantiaExtendidaException e) {
			// assert
			Assert.assertEquals(Vendedor.EL_PRODUCTO_TIENE_GARANTIA, e.getMessage());
		}
	}
	
	@Test
	public void generarGarantiaDe200DiasTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conPrecio(PRECIO_PRODUCTO_TEST_200_DIAS).build();		
		repositorioProducto.agregar(producto);
		
		Calendar calendario = Calendar.getInstance();		
		calendario.set(Calendar.YEAR, 2018);
		calendario.set(Calendar.MONTH, 8-1);
		calendario.set(Calendar.DAY_OF_MONTH, 16);
		Date fechaSolicitud = calendario.getTime();
		
		//En el PDF aparece que con Fecha Solicitud Garantia:16/08/2018 daría Fecha Fin Garantía: 06/04/2019 
		// pero haciendo prueba y error manualmente la respuesta verdadera es Fecha Fin Garantía: 05/04/2019 
		calendario.set(Calendar.YEAR, 2019);
		calendario.set(Calendar.MONTH, 4-1);
		calendario.set(Calendar.DAY_OF_MONTH, 5);
		Date fechaFinGarantia = calendario.getTime();
		
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia, fechaSolicitud);

		// act
		vendedor.generarGarantia(producto.getCodigo(), DANILO_ROMAN);

		// assert
		Assert.assertEquals(PRECIO_GARANTIA_TEST_200_DIAS,  repositorioGarantia.obtener(producto.getCodigo()).getPrecioGarantia(), 0);
		Assert.assertEquals(fechaFinGarantia, repositorioGarantia.obtener(producto.getCodigo()).getFechaFinGarantia());

	}
	
	@Test
	public void generarGarantiaDe200DiasCayendoDomingoTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conPrecio(PRECIO_PRODUCTO_TEST_200_DIAS_CAYENDO_DOMINGO).build();		
		repositorioProducto.agregar(producto);
		
		Calendar calendario = Calendar.getInstance();		
		calendario.set(Calendar.YEAR, 2018);
		calendario.set(Calendar.MONTH, 8-1);
		calendario.set(Calendar.DAY_OF_MONTH, 18);
		Date fechaSolicitud = calendario.getTime();
		
		calendario.set(Calendar.YEAR, 2019);
		calendario.set(Calendar.MONTH, 4-1);
		calendario.set(Calendar.DAY_OF_MONTH, 9);
		Date fechaFinGarantia = calendario.getTime();
		
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia, fechaSolicitud);

		// act
		vendedor.generarGarantia(producto.getCodigo(), DANILO_ROMAN);

		// assert
		Assert.assertEquals(PRECIO_GARANTIA_TEST_200_DIAS_CAYENDO_DOMINGO,  repositorioGarantia.obtener(producto.getCodigo()).getPrecioGarantia(), 0);
		Assert.assertEquals(fechaFinGarantia, repositorioGarantia.obtener(producto.getCodigo()).getFechaFinGarantia());

	}
	
	@Test
	public void generarGarantiaDe100DiasTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conPrecio(PRECIO_PRODUCTO_TEST_100_DIAS).build();		
		repositorioProducto.agregar(producto);
		
		Calendar calendario = Calendar.getInstance();		
		calendario.set(Calendar.YEAR, 2019);
		calendario.set(Calendar.MONTH, 1-1);
		calendario.set(Calendar.DAY_OF_MONTH, 1);
		Date fechaSolicitud = calendario.getTime();
		
		calendario.set(Calendar.YEAR, 2019);
		calendario.set(Calendar.MONTH, 4-1);
		calendario.set(Calendar.DAY_OF_MONTH, 26);
		Date fechaFinGarantia = calendario.getTime();
		
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia, fechaSolicitud);

		// act
		vendedor.generarGarantia(producto.getCodigo(), DANILO_ROMAN);

		// assert
		Assert.assertEquals(PRECIO_GARANTIA_TEST_100_DIAS,  repositorioGarantia.obtener(producto.getCodigo()).getPrecioGarantia(), 0);
		Assert.assertEquals(fechaFinGarantia, repositorioGarantia.obtener(producto.getCodigo()).getFechaFinGarantia());

	}
	
	@Test
	public void generarGarantiaDe100DiasCayendoDomingoTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conPrecio(PRECIO_PRODUCTO_TEST_100_DIAS_CAYENDO_DOMINGO).build();		
		repositorioProducto.agregar(producto);
		
		Calendar calendario = Calendar.getInstance();		
		calendario.set(Calendar.YEAR, 2018);
		calendario.set(Calendar.MONTH, 12-1);
		calendario.set(Calendar.DAY_OF_MONTH, 27);
		Date fechaSolicitud = calendario.getTime();
		
		calendario.set(Calendar.YEAR, 2019);
		calendario.set(Calendar.MONTH, 4-1);
		calendario.set(Calendar.DAY_OF_MONTH, 23);
		Date fechaFinGarantia = calendario.getTime();
		
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia, fechaSolicitud);

		// act
		vendedor.generarGarantia(producto.getCodigo(), DANILO_ROMAN);

		// assert
		Assert.assertEquals(PRECIO_GARANTIA_TEST_100_DIAS_CAYENDO_DOMINGO,  repositorioGarantia.obtener(producto.getCodigo()).getPrecioGarantia(), 0);
		Assert.assertEquals(fechaFinGarantia, repositorioGarantia.obtener(producto.getCodigo()).getFechaFinGarantia());

	}
	
	@Test
	public void productoSinGarantiaExtendida() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conCodigo(CODIGO_PRODUCTO_CON_TRES_VOCALES).build();
		
		repositorioProducto.agregar(producto);
		
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia, new Date());

		// act
		try {
			
			vendedor.generarGarantia(producto.getCodigo(), DANILO_ROMAN);
			fail();
			
		} catch (GarantiaExtendidaException e) {
			// assert
			Assert.assertEquals(Vendedor.ESTE_PRODUCTo_HO_CUENTA_CON_GARANTIA_EXTENDIDA, e.getMessage());
			Assert.assertNull(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo()));
		}
	}
	
	@Test
	public void noSeEncuentraProducto() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().build();
		
		repositorioProducto.agregar(producto);
		
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia, new Date());

		// act
		try {
			
			vendedor.generarGarantia(CODIGO_PRODUCTO_ERRONEO, DANILO_ROMAN);
			fail();
			
		} catch (GarantiaExtendidaException e) {
			// assert
			Assert.assertEquals(Vendedor.NO_SE_ENCUETRA_UN_PRODUCTO_CON_ESTE_CODIGO, e.getMessage());
			Assert.assertNull(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo()));
		}
	}
}
