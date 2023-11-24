from PIL import Image
import json

images_path = "../src/main/resources/assets/mini_tardis/textures/item/"
models_path = "../src/main/resources/assets/mini_tardis/models/item/"

for i in range(0, 16):
    image = images_path + "tardis.png"
    dest = images_path + "tardis_alpha_" + str(i) + ".png"
    img = Image.open(image)
    img.putalpha(int(256 / 16 * i))
    img.save(dest)

    model = {
              "parent": "mini_tardis:item/tardis",
              "textures": {
                "0": "mini_tardis:item/tardis_alpha_" + str(i)
              }
            }
    with open(models_path + "tardis_alpha_" + str(i) + ".json", 'w', encoding='utf-8') as f:
        json.dump(model, f, ensure_ascii=False, indent=4)