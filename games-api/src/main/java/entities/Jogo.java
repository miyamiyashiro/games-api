package entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class Jogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório") // Bean Validation
    private String titulo;

    @ManyToOne
    @JoinColumn(name = "editora_id")
    private Editora editora; // Relacionamento Many-to-One
}

