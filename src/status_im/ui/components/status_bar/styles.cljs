(ns status-im.ui.components.status-bar.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

(defn- create-status-bar-style [{:keys [background-color bar-style translucent?]
                                 :or   {bar-style "light-content"}}]
  {:background-color (if translucent? "transparent" background-color)
   :translucent      translucent?
   :bar-style        bar-style})

(styles/def status-bar-default
  {:ios     (create-status-bar-style {:background-color colors/white
                                      :bar-style        "default"})
   :android (create-status-bar-style {:translucent?     true
                                      :bar-style        "dark-content"})})

(styles/def status-bar-transparent
  {:ios     (create-status-bar-style {:background-color colors/transparent})
   :android (create-status-bar-style {:translucent?     true})})