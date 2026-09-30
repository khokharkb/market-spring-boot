package org.example.market.favoris;
import org.example.market.annonce.Annonce;
import org.example.market.annonce.AnnonceRepository;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
import org.example.market.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriService {

    @Autowired
    private FavoriRepository favoriRepository;

    @Autowired
    private AnnonceRepository annonceRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // Ajouter un favori
    @Transactional
    public Favori ajouterFavori(Long annonceId, User user) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        // Vérifier si déjà en favoris
        if (favoriRepository.existsByUserAndAnnonce(user, annonce)) {
            throw new RuntimeException("Cette annonce est déjà dans vos favoris");
        }

        Favori favori = new Favori(user, annonce);
        return favoriRepository.save(favori);
    }

    @Transactional
    public void supprimerFavori(Long annonceId, User user) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        favoriRepository.deleteByUserAndAnnonce(user, annonce);
    }

    // Obtenir tous les favoris d'un utilisateur
    public List<Favori> getFavorisByUser(User user) {
        return favoriRepository.findFavorisWithAnnonceByUser(user);
    }

    // Vérifier si une annonce est dans les favoris
    public boolean isFavori(Long annonceId, User user) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        return favoriRepository.existsByUserAndAnnonce(user, annonce);
    }

    // Compter le nombre de favoris d'une annonce
    public Long countFavorisByAnnonce(Long annonceId) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        return favoriRepository.countByAnnonce(annonce);
    }

    // Supprimer un favori par son ID
    @Transactional
    public void supprimerFavoriById(Long favoriId, User user) {
        Favori favori = favoriRepository.findById(favoriId)
                .orElseThrow(() -> new RuntimeException("Favori non trouvé"));

        // Vérifier que l'utilisateur est propriétaire du favori
        if (!favori.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce favori");
        }

        favoriRepository.delete(favori);
    }
    public List<Long> getFavoriteAnnonceIds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        List<Favori> favoris = getFavorisByUser(user);
        return favoris.stream()
                .map(favori -> favori.getAnnonce().getId())
                .collect(Collectors.toList());
    }
}