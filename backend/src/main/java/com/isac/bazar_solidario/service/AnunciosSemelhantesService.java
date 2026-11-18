package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.AnuncioRequest;
import com.isac.bazar_solidario.repository.AnuncioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.*;
import java.security.*;
import java.text.Normalizer;
import java.util.*;

@Service
public class AnunciosSemelhantesService {
    public record Sugestao(Long id,String titulo,String codigoPeca,String estado,List<String> motivos){}
    private final AnuncioRepository anuncios;
    private final Path uploads;
    @org.springframework.beans.factory.annotation.Autowired private com.isac.bazar_solidario.repository.ArquivoFotoRepository fotos;
    public AnunciosSemelhantesService(AnuncioRepository anuncios,@Value("${app.uploads.directory:uploads}") String uploads){
        this.anuncios=anuncios;this.uploads=Path.of(uploads).toAbsolutePath().normalize();
    }
    public List<Sugestao> buscar(AnuncioRequest req,Long vendedorId){
        var fotos=new HashSet<String>();
        if(req.imagens()!=null)req.imagens().forEach(url->fotos.add(fingerprint(url)));
        return anuncios.findByVendedorId(vendedorId).stream().map(a->{
            var motivos=new ArrayList<String>();
            if(a.getCategoria().getId().equals(req.categoriaId()) && normalizar(a.getTitulo()).equals(normalizar(req.titulo())))motivos.add("Mesmo título e categoria");
            if(a.getImagens().stream().anyMatch(i->fotos.contains(fingerprint(i.getUrl()))))motivos.add("Mesma foto");
            return new Sugestao(a.getId(),a.getTitulo(),a.getCodigoPeca()!=null?a.getCodigoPeca():"LEG-"+a.getId(),a.getStatus().name(),motivos);
        }).filter(a->!a.motivos().isEmpty()).limit(5).toList();
    }
    private static String normalizar(String valor){return Normalizer.normalize(valor,Normalizer.Form.NFD)
        .replaceAll("\\p{M}","").trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT);}
    private String fingerprint(String url){
        if(url.matches("/uploads/[a-f0-9-]{36}\\.(jpg|png|webp)")){
            if(fotos!=null) {
                var foto=fotos.findById(url.substring(9));
                if(foto.isPresent())try{return "sha256:"+HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(foto.get().getConteudo()));}
                catch(NoSuchAlgorithmException ignored) {throw new IllegalStateException("SHA-256 indisponível.");}
            }
            var arquivo=uploads.resolve(url.substring(9)).normalize();
            try{
                if(arquivo.startsWith(uploads) && !Files.isSymbolicLink(arquivo) && Files.isRegularFile(arquivo) && Files.size(arquivo)<=5L*1024*1024)
                    return "sha256:"+HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(arquivo)));
            }catch(java.io.IOException|NoSuchAlgorithmException ignored){/* Um arquivo ausente não bloqueia a comparação de título. */}
        }
        return "url:"+url;
    }
}
