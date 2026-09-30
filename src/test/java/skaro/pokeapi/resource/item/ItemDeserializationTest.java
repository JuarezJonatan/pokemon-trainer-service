package skaro.pokeapi.resource.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;

import skaro.pokeapi.PokeApiReactorBaseConfiguration;

class ItemDeserializationTest {

	@Test
	void givenARealPokeApiItemResponse_whenDecodingItWithTheLibraryDecoder_thenTheCategoryIsASingleResource() throws Exception {
		byte[] json = new ClassPathResource("pokeapi/item-ultra-ball.json").getContentAsByteArray();
		DataBuffer buffer = DefaultDataBufferFactory.sharedInstance.wrap(json);

		Item item = (Item) new PokeApiReactorBaseConfiguration().jsonDecoder()
				.decode(buffer, ResolvableType.forClass(Item.class), MediaType.APPLICATION_JSON, Map.of());

		assertThat(item.getName()).isEqualTo("ultra-ball");
		assertThat(item.getCategory().getName()).isEqualTo("standard-balls");
	}

}
