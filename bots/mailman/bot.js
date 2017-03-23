status.command({
    name: "location",
    icon: "location",
    title: I18n.t('location_title'),
    description: I18n.t('location_description'),
    color: "#a187d5",
    preview: function (params) {
        var text = status.components.text(
            {
                style: {
                    marginTop: 5,
                    marginHorizontal: 0,
                    fontSize: 14,
                    fontFamily: "font",
                    color: "black"
                }
            }, params.address);
        var uri = "https://maps.googleapis.com/maps/api/staticmap?center="
            + params.address
            + "&size=100x100&maptype=roadmap&key=AIzaSyBNsj1qoQEYPb3IllmWMAscuXW0eeuYqAA&language=en"
            + "&markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C"
            + params.address;

        var image = status.components.image(
            {
                source: {uri: uri},
                style: {
                    width: 100,
                    height: 100
                }
            }
        );

        return status.components.view({}, [text, image]);
    }
}).param({
    name: "address",
    type: status.types.TEXT,
    placeholder: I18n.t('location_address')
});
