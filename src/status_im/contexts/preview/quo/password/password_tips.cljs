(ns status-im.contexts.preview.quo.password.password-tips
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def tips [:lower-case? :upper-case? :numbers? :symbols?])

(defn make-init-state
  []
  (->> (repeat 4 false)
       (vec)
       (zipmap tips)))

(defn make-tip-descriptor
  [tip]
  {:key  tip
   :type :boolean})

(def descriptor
  (map make-tip-descriptor tips))

(defn view
  []
  (let [state (reagent/atom (make-init-state))]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding          20
                                    :background-color colors/neutral-95}}
       [quo/password-tips @state]])))
