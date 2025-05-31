(ns quo.components.avatars.wallet-user-avatar.view
  (:require [clojure.string :as string]
            [quo.components.avatars.wallet-user-avatar.style :as style]
            [quo.components.markdown.text :as text]
            [quo.context :as quo.context]
            [react-native.core :as rn]
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
(def second-smallest-possible (second (keys properties)))
(defn check-if-size-small
  [size]
  (or (= size smallest-possible)
      (= size second-smallest-possible)))
(def biggest-possible (last (keys properties)))

(defn wallet-user-avatar
  "Options:

  :full-name - string (default: nil) - used to generate initials
  :customization-color  - keyword (default: nil) - color of the avatar
  :size   - keyword (default: last element of properties object) - size of the
  avatar
  :monospace? - boolean (default: false) - use monospace font
  :lowercase? - boolean (default: false) - lowercase text
  :neutral? - boolean (default: false) - use neutral colors variant"
  [{:keys [full-name customization-color size monospace? lowercase? neutral? container-style]
    :or   {size biggest-possible}}]
  (let [theme       (quo.context/use-theme)
        circle-size (:size (size properties))
        small?      (check-if-size-small size)
        initials    (utils.string/get-initials full-name (if small? 1 2))]
    [rn/view
     {:style (merge (style/container circle-size customization-color neutral? theme) container-style)}
     [text/text
      {:accessibility-label :wallet-user-avatar
       :size                (:font-size (size properties))
       :weight              (if monospace? :monospace (:font-weight (size properties)))
       :style               (style/text customization-color neutral? theme)}
      (if (and initials lowercase?) (string/lower-case initials) initials)]]))
