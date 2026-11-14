package pagoflex.tarjetas.service;
import pagoflex.tarjetas.dto.TarjetaRequest;
import pagoflex.tarjetas.dto.TarjetaResponse;
import pagoflex.tarjetas.model.Tarjeta;
import pagoflex.tarjetas.repository.TarjetaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class TarjetaService {
    private static final int MAX_TARJETAS = 4;
    private final TarjetaRepository repository;
    public TarjetaService(TarjetaRepository repository) { this.repository = repository; }

    public TarjetaResponse agregar(TarjetaRequest req) {
        if (repository.countByUsuarioId(req.getUsuarioId()) >= MAX_TARJETAS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo " + MAX_TARJETAS + " tarjetas");
        }
        if (req.isPrincipal()) {
            repository.findByUsuarioId(req.getUsuarioId()).forEach(t -> {
                t.setPrincipal(false);
                repository.save(t);
            });
        }
        Tarjeta t = Tarjeta.builder()
                .usuarioId(req.getUsuarioId())
                .ultimosDigitos(req.getUltimosDigitos())
                .titular(req.getTitular())
                .vencimiento(req.getVencimiento())
                .marca(req.getMarca() != null ? req.getMarca() : "Tarjeta")
                .principal(req.isPrincipal())
                .build();
        return toResponse(repository.save(t));
    }

    public List<TarjetaResponse> listar(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TarjetaResponse marcarPrincipal(Long id) {
        Tarjeta t = buscar(id);
        repository.findByUsuarioId(t.getUsuarioId()).forEach(x -> {
            x.setPrincipal(x.getId().equals(id));
            repository.save(x);
        });
        t.setPrincipal(true);
        return toResponse(repository.save(t));
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarjeta no encontrada");
        }
        repository.deleteById(id);
    }

    private Tarjeta buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarjeta no encontrada"));
    }

    private TarjetaResponse toResponse(Tarjeta t) {
        return TarjetaResponse.builder()
                .id(t.getId()).usuarioId(t.getUsuarioId())
                .ultimosDigitos(t.getUltimosDigitos()).titular(t.getTitular())
                .vencimiento(t.getVencimiento()).marca(t.getMarca())
                .principal(t.isPrincipal()).build();
    }
}
