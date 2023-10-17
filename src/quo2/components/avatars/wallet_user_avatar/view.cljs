(ns quo2.components.avatars.wallet-user-avatar.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [quo2.components.avatars.wallet-user-avatar.style :as style]
            utils.string))

(def properties
  {:size-20 {:size        20
             :font-size   :label
             :font-weight :medium}
   :size-24 {:size        24
             :font-size   :label
             :font-weight :semi-bold}
   :size-32 {:size        32
             :font-size   :paragraph-2
             :font-weight :semi-bold}
   :size-48 {:size        48
             :font-size   :paragraph-1
             :font-weight :semi-bold}
   :size-64 {:size        64
             :font-size   :paragraph-1
             :font-weight :medium}
   :size-80 {:size        80
             :font-size   :heading-1
             :font-weight :medium}})

(def smallest-possible (first (keys properties)))
(def biggest-possible (last (keys properties)))

(defn- view-internal
  "Options:

  :full-name - string (default: nil) - used to generate initials
  :customization-color  - keyword (default: nil) - color of the avatar
  :size   - keyword (default: last element of properties object) - size of the
  avatar
  :monospace? - boolean (default: false) - use monospace font"
  [{:keys [full-name customization-color size theme monospace?]
    :or   {size biggest-possible}}]
  (let [circle-size (:size (size properties))
        small?      (= size smallest-possible)]
    [rn/view
     {:style (style/container circle-size customization-color)}
     [text/text
      {:accessibility-label :wallet-user-avatar
       :size                (:font-size (size properties))
       :weight              (if monospace? :monospace (:font-weight (size properties)))
       :style               (style/text customization-color theme)}
      (utils.string/get-initials full-name (if small? 1 2))]]))

(def wallet-user-avatar (quo.theme/with-theme view-internal))
