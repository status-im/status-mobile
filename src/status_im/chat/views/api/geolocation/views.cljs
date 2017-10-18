(ns status-im.chat.views.api.geolocation.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]]
                   [reagent.ratom :refer [reaction]])
  (:require [status-im.ui.components.react :refer [view image text touchable-highlight]]
            [reagent.core :as r]
            [goog.string :as gstr]
            [status-im.utils.utils :refer [http-get]]
            [status-im.utils.types :refer [json->clj]]
            [status-im.chat.views.api.geolocation.styles :as st]
            [status-im.ui.components.mapbox :refer [mapview]]
            [re-frame.core :refer [dispatch subscribe]]
            [status-im.i18n :refer [label]]
            [status-im.ui.components.react :as components]))

(def mapbox-api "https://api.mapbox.com/geocoding/v5/mapbox.places/")
(def access-token "pk.eyJ1Ijoic3RhdHVzaW0iLCJhIjoiY2oydmtnZjRrMDA3czMzcW9kemR4N2lxayJ9.Rz8L6xdHBjfO8cR3CDf3Cw")

(defn get-places [coords cur-loc-geocoded & [poi?]]
  (let [{:keys [latitude longitude]} coords]
    (when (and latitude longitude)
      (http-get (str mapbox-api longitude "," latitude
                     ".json?" (when poi? "types=poi&") "access_token=" access-token)
                #(reset! cur-loc-geocoded (json->clj %))
                #(reset! cur-loc-geocoded nil))
      true)))

(defn get-coord [{:keys [latitude longitude]}]
  {:latitude (or latitude 0)
   :longitude (or longitude 0)})

(defn place-item [{:keys [title address pin-style] [latitude longitude] :center}]
  [touchable-highlight {:on-press #(do
                                     (dispatch [:set-command-argument [0
                                                                       (str (or address title)
                                                                            "&amp;" latitude
                                                                            "&amp;" longitude)
                                                                       false]])
                                     (dispatch [:send-seq-argument]))}
   [view (st/place-item-container address)
    [view st/place-item-title-container
     [view (st/place-item-circle-icon pin-style)]
     [text {:style st/place-item-title
            :number-of-lines 1
            :font :medium}
      title]]
    (when address
      [text {:style st/place-item-address
             :number-of-lines 1}
       address])]])

(defview current-location-map-view []
  (letsubs [geolocation [:get :geolocation]
            command     [:selected-chat-command]]
    {:component-will-mount #(dispatch [:request-geolocation-update])
     :component-did-mount (fn [_] (js/setTimeout #(dispatch [:chat-input-focus :seq-input-ref]) 400))}
    (let [coord (select-keys (:coords geolocation) [:latitude :longitude])]
      [view
       (if (not (empty? coord))
         [view
          [mapview {:onTap #(do
                              (dispatch [:set-command-argument [0 "Dropped pin" false]])
                              (dispatch [:set-chat-seq-arg-input-text "Dropped pin"])
                              (dispatch [:chat-input-blur :seq-input-ref])
                              (dispatch [:load-chat-parameter-box (:command command)]))
                    :initialCenterCoordinate (get-coord coord)
                    :showsUserLocation true
                    :initialZoomLevel 10
                    :logoIsHidden true
                    :rotateEnabled false
                    :scrollEnabled false
                    :zoomEnabled false
                    :pitchEnabled false
                    :style st/map-view}]]
         [view st/map-activity-indicator-container
          [components/activity-indicator {:animating true}]])])))

(defn current-location-view []
  (let [geolocation      (subscribe [:get :geolocation])
        cur-loc-geocoded (r/atom nil)
        result (reaction (when @geolocation (get-places (:coords @geolocation) cur-loc-geocoded)))]
    (r/create-class
      {:component-will-mount #(dispatch [:request-geolocation-update])
       :display-name "current-location-view"
       :reagent-render
       (fn []
         (let [_ @result]
           (when (and @cur-loc-geocoded (> (count (:features @cur-loc-geocoded)) 0))
             [view st/location-container
              [text {:style st/location-container-title}
                   (label :t/your-current-location)]
                  (let [{:keys [place_name center] :as feature} (get-in @cur-loc-geocoded [:features 0])]
                    [place-item {:title (:text feature) :address place_name :center center}])])))})))

(defn places-nearby-view []
  (let [geolocation      (subscribe [:get :geolocation])
        cur-loc-geocoded (r/atom nil)
        result (reaction (when @geolocation (get-places (:coords @geolocation) cur-loc-geocoded true)))]
    (r/create-class
      {:component-will-mount #(dispatch [:request-geolocation-update])
       :render
         (fn []
           (let [_ @result]
             (when (and @cur-loc-geocoded (> (count (:features @cur-loc-geocoded)) 0))
               [view st/location-container
                [text {:style st/location-container-title}
                 (label :t/places-nearby)]
                (doall
                  (map (fn [{:keys [text place_name center] :as feature}]
                         ^{:key feature}
                         [view
                          [place-item {:title text :address place_name :center center}]
                          (when (not= feature (last (:features @cur-loc-geocoded)))
                            [view st/item-separator])])
                       (:features @cur-loc-geocoded)))])))})))

(defn places-search []
  (let [seq-arg-input-text (subscribe [:chat :seq-argument-input-text])
        places             (r/atom nil)
        result             (reaction (http-get (str mapbox-api @seq-arg-input-text
                                                    ".json?access_token=" access-token)
                                               #(reset! places (json->clj %))
                                               #(reset! places nil)))]
    (fn []
      (let [_ @result]
        (when @places
          (let [features-count (count (:features @places))]
            [view st/location-container
             [text {:style st/location-container-title}
              (label :t/search-results) " " [text {:style st/location-container-title-count} features-count]]
             (doall
               (map (fn [{:keys [place_name center] :as feature}]
                      ^{:key feature}
                      [view
                       [place-item {:title place_name :center center}]
                       (when (not= feature (last (:features @places)))
                         [view st/item-separator])])
                    (:features @places)))]))))))

(defn dropped-pin []
  (let [geolocation     @(subscribe [:get :geolocation])
        pin-location    (r/atom nil)
        pin-geolocation (r/atom nil)
        pin-nearby      (r/atom nil)
        result          (reaction (when @pin-location (get-places @pin-location pin-geolocation)))
        result2         (reaction (when @pin-location (get-places @pin-location pin-nearby true)))]
    (fn []
      (let [_ @result _ @result2
            coord (select-keys (:coords geolocation) [:latitude :longitude])]
        [view
         [view
          [mapview {:initial-center-coordinate (get-coord coord) 
                    :initialZoomLevel 10
                    :onRegionDidChange #(reset! pin-location (js->clj % :keywordize-keys true))
                    :logoIsHidden true
                    :style {:height 265}}]
          [view st/pin-container
           [view st/pin-component
            [view st/pin-circle]
            [view st/pin-leg]]]]
         (when (and @pin-geolocation (> (count (:features @pin-geolocation)) 0))
           [view
            [view st/location-container
             [text {:style st/location-container-title}
              (label :t/dropped-pin)]
             (let [{:keys [place_name center] :as feature} (get-in @pin-geolocation [:features 0])]
               [place-item {:title place_name :pin-style st/black-pin :center center}])]
            [view st/separator]])
         (when (and @pin-nearby (> (count (:features @pin-nearby)) 0))
           [view st/location-container
            [text {:style st/location-container-title}
             (label :t/places-nearby)]
            (doall
              (map (fn [{:keys [text place_name center] :as feature}]
                     ^{:key feature}
                     [view
                      [place-item {:title text :address place_name :center center}]
                      (when (not= feature (last (:features @pin-nearby)))
                        [view st/item-separator])])
                   (:features @pin-nearby)))])]))))
