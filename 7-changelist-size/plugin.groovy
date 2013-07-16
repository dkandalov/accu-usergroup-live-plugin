import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diff.impl.ComparisonPolicy
import com.intellij.openapi.diff.impl.fragments.LineFragment
import com.intellij.openapi.diff.impl.processing.DiffPolicy
import com.intellij.openapi.diff.impl.processing.TextCompareProcessor
import com.intellij.openapi.diff.impl.util.TextDiffTypeEnum
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeList
import com.intellij.openapi.vcs.changes.ChangeListManager
import org.jetbrains.annotations.Nullable

import static com.intellij.openapi.diff.impl.util.TextDiffTypeEnum.CHANGED
import static com.intellij.openapi.diff.impl.util.TextDiffTypeEnum.CONFLICT
import static com.intellij.openapi.diff.impl.util.TextDiffTypeEnum.DELETED
import static com.intellij.openapi.diff.impl.util.TextDiffTypeEnum.INSERT
import static liveplugin.PluginUtil.*

// this is "source code as interface" approach
// (change code below to configure change size limit and notification interval)
int changedLinesLimit = 20
int delayBetweenNotificationsInSeconds = 60
boolean isWatching = false

registerAction("WatchChangeListSize", "alt shift W") { AnActionEvent event ->
    def project = event.project
    def notificationDelay = 0

    def checkChangeListSize = {
        int linesChanged = amountOfChangedLinesIn(ChangeListManager.getInstance(project).defaultChangeList)
        if (linesChanged > changedLinesLimit) {
            if (notificationDelay <= 0) {
                def title = "Change Limit Exceeded"
                def message = "Lines changed: ${linesChanged}; Limit: ${changedLinesLimit}<br/>Please consider commiting or reverting changes"
                show(message, title, NotificationType.WARNING)
                notificationDelay = delayBetweenNotificationsInSeconds
            } else {
                notificationDelay--
            }
        } else {
            notificationDelay = 0
        }
    }

    if (!isWatching) {
        isWatching = true
        new Thread({
            show("Started watching change list size")
            while (isWatching) {
                sleep(1000)
                checkChangeListSize()
            }
            show("Stopped watching change list size")
        }).start()
    } else {
        isWatching = false
    }
}
show("Use 'alt shift W' to start/stop watching default change list size")


static int amountOfChangedLinesIn(ChangeList changeList) {
    def compareProcessor = new TextCompareProcessor(ComparisonPolicy.IGNORE_SPACE)
    changeList.changes.sum(0) { change ->
        if (change.beforeRevision == null) return change.afterRevision.content.split("\n").size()
        if (change.afterRevision == null) return change.beforeRevision.content.split("\n").size()

        def lineFragments = compareProcessor.process(change.beforeRevision.content, change.afterRevision.content)
        lineFragments.sum { LineFragment fragment ->
            fragment.with {
                if (type == DELETED) modifiedLines1
                else if (type in [CHANGED, INSERT, CONFLICT]) modifiedLines2
                else 0
            }
        }
    }
}