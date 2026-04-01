package entities;
@Entity
@Data
public class Editora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 2)
    private String nome;

    @OneToMany(mappedBy = "editora") // Relacionamento One-to-Many
    private List<Jogo> jogos;
}
}
