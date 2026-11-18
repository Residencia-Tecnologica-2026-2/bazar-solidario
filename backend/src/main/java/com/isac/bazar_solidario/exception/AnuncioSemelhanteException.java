package com.isac.bazar_solidario.exception;
import com.isac.bazar_solidario.service.AnunciosSemelhantesService.Sugestao;
import java.util.List;
public class AnuncioSemelhanteException extends RuntimeException {
    private final List<Sugestao> sugestoes;
    public AnuncioSemelhanteException(List<Sugestao> sugestoes){super("Encontramos anúncios parecidos na sua loja. Confira antes de publicar.");this.sugestoes=sugestoes;}
    public List<Sugestao> getSugestoes(){return sugestoes;}
}
