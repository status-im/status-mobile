(ns ^:figwheel-no-load env.android.worker
  (:require [figwheel.client :as figwheel]
            [re-frisk-remote.core :as rr]
            [env.config :as conf]
            [env.utils]
            [status-im.worker.core :as core]))

(enable-console-print!)

(figwheel/start {:websocket-url    (:android conf/figwheel-urls)
                 :heads-up-display false})

(.define js/goog "status_im.utils.config.thread" "handlers")

(rr/enable-re-frisk-remote! {:host (env.utils/re-frisk-url (:android conf/figwheel-urls))
                             :on-init (core/init)})
