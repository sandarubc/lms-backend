package lk.ijse.dep9.lmsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDTO implements Serializable {

    private String memberID;
    private List<ReturnItemDTO> returnItems= new ArrayList<>();
}
