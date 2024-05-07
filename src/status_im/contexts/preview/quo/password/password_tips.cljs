(ns status-im.contexts.preview.quo.password.password-tips
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [reagent.core :as reagent]
    [status-im.constants :as constant]
    [status-im.contexts.preview.quo.preview :as preview]))

(def init-state
  (reduce (fn [acc tip] (assoc acc tip false)) {} constant/password-tips))

(defn- make-tip-descriptor
  [tip]
  {:key  tip
   :type :boolean})

(def descriptor
  (map make-tip-descriptor constant/password-tips))

(defn view
  []
  (let [state (reagent/atom init-state)]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding          20
                                    :background-color colors/neutral-95}}
       [quo/password-tips @state]])))
