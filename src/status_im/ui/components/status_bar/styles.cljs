(ns status-im.ui.components.status-bar.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

(defn- create-status-bar-style [{:keys [background-color bar-style translucent?]
                                 :or   {bar-style "light-content"}}]
  {:background-color (if translucent? "transparent" background-color)
   :translucent      translucent?
   :bar-style        bar-style})

(styles/defn status-bar-default []
  {:ios     (create-status-bar-style (when-not (colors/dark?)
                                       {:bar-style "dark-content"}))
   :android (create-status-bar-style (if (colors/dark?)
                                       {:translucent? true}
                                       {:translucent? true
                                        :bar-style "dark-content"}))})

(styles/defn status-bar-black []
  {:ios     (create-status-bar-style nil)
   :android (create-status-bar-style (if (colors/dark?)
                                       {:translucent? true}
                                       {:background-color colors/black}))})
