(ns status-im2.navigation.screens
  (:require [status-im2.setup.config :as config]
            [status-im.ui2.screens.quo2-preview.main :as quo2.preview]
            [status-im.ui2.screens.chat.home :as chat.home]
            [status-im.ui2.screens.chat.view :as chat]
            [status-im.ui2.screens.communities.discover-communities :as discover-communities]
            [status-im2.contexts.shell.view :as shell]

            ;; TODO remove when not used anymore
            [status-im.ui.screens.screens :as old-screens]))

(def components
  [])

(defn screens []
  (concat (old-screens/screens)

          [{:name      :shell-stack
            :insets    {:top false}
            :component shell/shell-stack}

           {:name      :home
            :component chat.home/home}

           {:name      :chat
            :options   {:topBar {:visible false}}
            :component chat/chat}

           {:name      :discover-communities
            :options   {:topBar {:visible false}}
            :component discover-communities/communities}]

          (when config/quo-preview-enabled?
            quo2.preview/screens)

          (when config/quo-preview-enabled?
            quo2.preview/main-screens)))
