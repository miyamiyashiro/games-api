package entities;
@Entity
@Data
public class Jogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String titulo;

    @NotNull
    private Integer minJogadores;

    @ManyToOne // O inverso do relacionamento
    @JoinColumn(name = "editora_id")
    private Editora editora;
}
}
