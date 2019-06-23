package dominio.integracion;

import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.Month;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dominio.GarantiaExtendida;
import dominio.Producto;
import dominio.Vendedor;
import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioGarantiaExtendida;
import dominio.repositorio.RepositorioProducto;
import persistencia.sistema.SistemaDePersistencia;
import testdatabuilder.ProductoTestDataBuilder;

public class VendedorTest {

	private static final String COMPUTADOR_LENOVO = "Computador Lenovo";
	private static final String NOMBRE_CLIENTE = "Gustavo Castro";
	private static final String CODIGO_SIN_GARANTIA = "CEIBA20190619";

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
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);

		// assert
		Assert.assertTrue(vendedor.tieneGarantia(producto.getCodigo()));
		Assert.assertNotNull(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo()));

	}

	@Test
	public void productoYaTieneGarantiaTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();

		repositorioProducto.agregar(producto);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);
		;
		try {

			vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);
			fail();

		} catch (GarantiaExtendidaException e) {
			// assert
			Assert.assertEquals(Vendedor.EL_PRODUCTO_TIENE_GARANTIA, e.getMessage());
		}
	}

	@Test // Test Regla de negocio #3
	public void productoNoPermiteGarantiaTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO_SIN_GARANTIA)
				.build();

		repositorioProducto.agregar(producto);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		try {
			vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);
			fail();
		} catch (GarantiaExtendidaException e) {
			// assert
			Assert.assertEquals(Vendedor.EL_PRODUCTO_NO_PERMITE_GARANTIA, e.getMessage());
		}
	}

	@Test // Test Regla de negocio #4
	public void comprobarDatosAlmacenadosGarantiaTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();

		repositorioProducto.agregar(producto);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);
		GarantiaExtendida garantia = repositorioGarantia.obtener(producto.getCodigo());

		Assert.assertNotNull(garantia.getProducto());
		Assert.assertNotNull(garantia.getNombreCliente());
		Assert.assertNotNull(garantia.getPrecioGarantia());
		Assert.assertNotNull(garantia.getFechaSolicitudGarantia());
		Assert.assertNotNull(garantia.getFechaFinGarantia());
	}

	@Test // Test Regla de negocio #5-1 - Precio Producto < 500000
	public void validarGarantiaMenor500Test() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conPrecio(499999l).build();

		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);

		GarantiaExtendida garantia = repositorioGarantia.obtener(producto.getCodigo());

		long valorGarantiaEsperado = 49999l;
		LocalDate fechaFinalGarantiaEsperado = LocalDate.of(2019, Month.SEPTEMBER, 30);

		// assert
		Assert.assertEquals(valorGarantiaEsperado, garantia.getPrecioGarantia(), 9);
		Assert.assertEquals(vendedor.convertLocalToDate(fechaFinalGarantiaEsperado), garantia.getFechaFinGarantia());

	}

	@Test // Test Regla de negocio #5-2 - Precio Producto = 500000
	public void validarGarantiaIgual500Test() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conPrecio(500000l).build();

		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);

		GarantiaExtendida garantia = repositorioGarantia.obtener(producto.getCodigo());

		long valorGarantiaEsperado = 50000l;

		// assert
		Assert.assertEquals(valorGarantiaEsperado, garantia.getPrecioGarantia(), 0);
	}

	@Test // Test Regla de negocio #5-3 - Precio Producto > 500000
	public void validarGarantiaMayor500Test() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conPrecio(650000l).build();

		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);

		GarantiaExtendida garantia = repositorioGarantia.obtener(producto.getCodigo());

		long valorGarantiaEsperado = 130000l;

		// assert
		Assert.assertEquals(valorGarantiaEsperado, garantia.getPrecioGarantia(), 0);
	}

	@Test // Test Regla de negocio #5-4 - Precio Producto < 500000
	public void validarDiasGarantiaMenor500Test() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conPrecio(300000l).build();

		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		int diasGarantia = vendedor.calcularDiasGarantia(producto.getPrecio());
		int diasGarantiaEsperados = 100;

		// assert
		Assert.assertEquals(diasGarantiaEsperados, diasGarantia);
	}

	@Test // Test Regla de negocio #5-5 - Precio Producto > 500000
	public void validarDiasGarantiaMayor500Test() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conPrecio(600000l).build();

		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		int diasGarantia = vendedor.calcularDiasGarantia(producto.getPrecio());
		int diasGarantiaEsperados = 200;

		// assert
		Assert.assertEquals(diasGarantiaEsperados, diasGarantia);
	}
}
