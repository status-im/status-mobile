(ns quo.components.profile.link-card.view
  (:require [quo.components.icon :as icons]
            [quo.components.markdown.text :as text]
            [quo.components.profile.link-card.properties :as properties]
            [quo.components.profile.link-card.style :as style]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.linear-gradient :as linear-gradient]
            [react-native.core :as rn]
            [quo.foundations.resources :as resources]))

(defn title
  [link]
  (case link
    :link "Website"
    :github "GitHub"
    :linkedin "LinkedIn"
    :superrare "SuperRare"
    :youtube "YouTube"
    link))

(defn website-icon
  [theme]
  [icons/icon
   :i/link
   {:accessibility-label :icon-right
    :color               (colors/theme-colors colors/neutral-50
                                              colors/neutral-40
                                              theme)
    :size                20
    :container-style {:margin-bottom 4}}])

;; (defn icon
;;   [link]
;;   (case link
;;     link))

(defn- view-internal
  [{:keys [address link theme]}]
  [linear-gradient/linear-gradient 
   {:colors   [(properties/gradient-start-color theme (if (= :link link) :army link))
               (properties/gradient-end-color theme (if (= :link link) :army link))]
    :start    {:x 0 :y 1}
    :end      {:x 1 :y 1}
    :style (style/container theme)}
  ;;  (if (= link :link)
  ;;    [website-icon]
  ;;    [rn/image
  ;;     {:style               {:width 20 :height 20}
  ;;      :source              ()
  ;;      :accessibility-label :illustration}]
  ;;    )
   [text/text 
    {:weight :semi-bold}
    (title link)]
   [text/text
    {:size :paragraph-2}
    address]])

(def view (quo.theme/with-theme view-internal))