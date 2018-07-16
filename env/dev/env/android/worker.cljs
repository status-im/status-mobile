(ns ^:figwheel-no-load env.android.worker
  (:require [figwheel.client :as figwheel]
            [env.config :as conf]
            [status-im.worker.core]))

(enable-console-print!)

(figwheel/start {:websocket-url    (:android conf/figwheel-urls)
                 :heads-up-display false})
