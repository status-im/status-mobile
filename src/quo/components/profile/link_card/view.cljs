(ns quo.components.profile.link-card.view
  (:require [clojure.core :as core]
            [clojure.string :as string]
            [quo.components.icon :as icons]
            [quo.components.markdown.text :as text]
            [quo.components.profile.link-card.properties :as properties]
            [quo.components.profile.link-card.style :as style]
            [quo.components.utilities.social.view :as social]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]))

(defn title
  [link]
  (case link
    :link      "Website"
    :github    "GitHub"
    :linkedin  "LinkedIn"
    :superrare "SuperRare"
    :youtube   "YouTube"
    (string/capitalize (core/name link))))

(defn website-icon
  [theme]
  [icons/icon
   :i/link
   {:accessibility-label :website-icon
    :color               (colors/theme-colors colors/neutral-50
                                              colors/neutral-40
                                              theme)
    :size                20}])

(defn- view-internal
  [{:keys [address link theme on-press customization-color]
    :or   {link :link}}]
  [rn/pressable
   {:accessibility-label :link-card
    :on-press            on-press}
   [linear-gradient/linear-gradient
    {:colors [(properties/gradient-start-color theme customization-color)
              (properties/gradient-end-color theme customization-color)]
     :start  {:x 0 :y 1}
     :end    {:x 1 :y 1}
     :style  (style/container theme)}
    [rn/view {:style style/icon-container}
     (if (= link :link)
       [website-icon]
       [social/view
        {:accessibility-label :social-icon
         :social              link}])]
    [text/text
     {:accessibility-label :title
      :weight              :semi-bold}
     (title link)]
    [text/text
     {:accessibility-label :address
      :size                :paragraph-2}
     address]]])

(def view (quo.theme/with-theme view-internal))
