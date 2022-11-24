package lk.ijse.dep9.lmsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemDTO implements Serializable {

    private String issuNoteId;
    private String isbn;

}
