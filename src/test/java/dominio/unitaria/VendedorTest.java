package dominio.unitaria;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;

import org.junit.Assert;
import org.junit.Test;

import dominio.Producto;
import dominio.Vendedor;
import dominio.repositorio.RepositorioGarantiaExtendida;
import dominio.repositorio.RepositorioProducto;
import testdatabuilder.ProductoTestDataBuilder;

public class VendedorTest {

	private static final String CODIGO_SIN_GARANTIA = "CEIBA20190619";
	private static final String NOMBRE_PRODUCTO = "Tablet Samsung 10";

	@Test
	public void productoYaTieneGarantiaTest() {

		// arrange
		ProductoTestDataBuilder productoTestDataBuilder = new ProductoTestDataBuilder();

		Producto producto = productoTestDataBuilder.build();

		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		when(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo())).thenReturn(producto);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		boolean existeProducto = vendedor.tieneGarantia(producto.getCodigo());

		// assert
		assertTrue(existeProducto);
	}

	@Test
	public void productoNoTieneGarantiaTest() {

		// arrange
		ProductoTestDataBuilder productoestDataBuilder = new ProductoTestDataBuilder();

		Producto producto = productoestDataBuilder.build();

		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		when(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo())).thenReturn(null);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		boolean existeProducto = vendedor.tieneGarantia(producto.getCodigo());

		// assert
		assertFalse(existeProducto);
	}

	@Test
	public void validarVocalesCodigoTest() {

		// arrange
		String codigoProducto = CODIGO_SIN_GARANTIA;
		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		int vocalesPorCodigo = vendedor.contarVocales(codigoProducto);
		int vocalesEsperadas = 3;

		// assert
		Assert.assertEquals(vocalesPorCodigo, vocalesEsperadas);
	}

	@Test
	public void validarFechaFinGarantiaTest() {
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(NOMBRE_PRODUCTO).conPrecio(640000l).build();
		LocalDate fechaSolicitud = LocalDate.of(2018, Month.AUGUST, 16);

		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		int diasGarantia = vendedor.calcularDiasGarantia(producto.getPrecio());
		LocalDate fechaFinGarantia = vendedor.calcularFechaFinGarantia(fechaSolicitud, diasGarantia);
		LocalDate fechaFinGarantiaEsperada = LocalDate.of(2019, Month.APRIL, 6);

		// assert
		Assert.assertEquals(fechaFinGarantiaEsperada, fechaFinGarantia);
	}

	@Test
	public void validarFechaFinGarantiaEnDomingoTest() {
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(NOMBRE_PRODUCTO).conPrecio(640000l).build();
		LocalDate fechaSolicitud = LocalDate.of(2018, Month.AUGUST, 17);

		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		int diasGarantia = vendedor.calcularDiasGarantia(producto.getPrecio());
		LocalDate fechaFinGarantia = vendedor.calcularFechaFinGarantia(fechaSolicitud, diasGarantia);
		LocalDate fechaFinGarantiaEsperada = LocalDate.of(2019, Month.APRIL, 8);

		// assert
		Assert.assertEquals(fechaFinGarantiaEsperada, fechaFinGarantia);
	}
}
