package dominio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioGarantiaExtendida;
import dominio.repositorio.RepositorioProducto;

public class Vendedor {

	public static final String EL_PRODUCTO_TIENE_GARANTIA = "El producto ya cuenta con una garantia extendida";
	public static final String EL_PRODUCTO_NO_PERMITE_GARANTIA = "Este producto no cuenta con garantia extendida";

	private RepositorioProducto repositorioProducto;
	private RepositorioGarantiaExtendida repositorioGarantia;

	public Vendedor(RepositorioProducto repositorioProducto, RepositorioGarantiaExtendida repositorioGarantia) {
		this.repositorioProducto = repositorioProducto;
		this.repositorioGarantia = repositorioGarantia;
	}

	public void generarGarantia(String codigo, String nombreCliente) {
		if (tieneGarantia(codigo))
			throw new GarantiaExtendidaException(EL_PRODUCTO_TIENE_GARANTIA);

		if (!permiteGarantia(codigo))
			throw new GarantiaExtendidaException(EL_PRODUCTO_NO_PERMITE_GARANTIA);

		Producto producto = repositorioProducto.obtenerPorCodigo(codigo);

		double precioGarantia = calcularPrecioGarantia(producto.getPrecio());
		int diasDeGarantia = calcularDiasGarantia(producto.getPrecio());

		LocalDate fechaSolicitudGarantia = LocalDate.now();
		LocalDate fechaFinGarantia = calcularFechaFinGarantia(fechaSolicitudGarantia, diasDeGarantia);

		GarantiaExtendida garantiaExtendida = new GarantiaExtendida(producto,
				convertLocalToDate(fechaSolicitudGarantia), convertLocalToDate(fechaFinGarantia), precioGarantia,
				nombreCliente);

		repositorioGarantia.agregar(garantiaExtendida);
	}

	public boolean tieneGarantia(String codigo) {
		Producto producto = repositorioGarantia.obtenerProductoConGarantiaPorCodigo(codigo);
		return producto != null ? true : false;
	}

	public double calcularPrecioGarantia(double precioProducto) {
		return precioProducto > 500000 ? precioProducto * 0.2 : precioProducto * 0.1;
	}

	public int calcularDiasGarantia(double precioProducto) {
		return precioProducto > 500000 ? 200 : 100;
	}

	public boolean permiteGarantia(String codigo) {
		return contarVocales(codigo) == 3 ? false : true;
	}

	public int contarVocales(String texto) {
		return (int) texto.toLowerCase().chars().mapToObj(i -> (char) i)
				.filter(c -> "aeiou".contains(String.valueOf(c))).count();
	}

	public LocalDate calcularFechaFinGarantia(LocalDate fechaSolicitud, int diasDeGarantia) {
		LocalDate fechaFinGarantia = fechaSolicitud.plusDays(diasDeGarantia);
		if (diasDeGarantia == 200) {
			while (fechaFinGarantia.isAfter(fechaSolicitud)) {
				if (DayOfWeek.MONDAY.equals(fechaSolicitud.getDayOfWeek())) {
					fechaFinGarantia = fechaFinGarantia.plusDays(1);
				}
				fechaSolicitud = fechaSolicitud.plusDays(1);
			}
			if (fechaFinGarantia.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
				fechaFinGarantia = fechaFinGarantia.plusDays(1);
			}
		}
		return fechaFinGarantia;
	}

	public Date convertLocalToDate(LocalDate dateToConvert) {
		return java.util.Date.from(dateToConvert.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

}
