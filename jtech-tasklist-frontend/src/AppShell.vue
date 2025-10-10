<template>
  <v-app>
    <v-navigation-drawer v-model="drawer" app>
      <v-list>
        <v-list-item @click="drawer = false">
          <v-list-item-title>Minhas Listas</v-list-item-title>
        </v-list-item>
        <v-list-item v-for="l in lists.items" :key="l.id" @click="goToTasks(l.id)">
          <v-list-item-title>{{ l.name }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar app>
      <v-btn v-if="auth.isAuthenticated" @click="drawer = !drawer" icon><i class="bi bi-list"></i></v-btn>
      <v-toolbar-title>TaskList App</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-menu v-if="auth.isAuthenticated" offset-y>
        <template #activator="{ props }">
          <v-btn v-bind="props" icon>
            <v-badge v-if="overdueCount > 0" :content="overdueCount" color="error" overlap>
              <i class="bi bi-bell-fill"></i>
            </v-badge>
            <i v-else class="bi bi-bell"></i>
          </v-btn>
        </template>
        <v-list style="min-width: 250px; max-width: 350px;">
          <v-list-item v-if="overdueTasks.length === 0">
            <v-list-item-title>Nenhuma tarefa vencida</v-list-item-title>
          </v-list-item>
          <v-list-item v-for="t in overdueTasks" :key="t.id" @click="goToOverdueTask(t)">
            <v-list-item-title>{{ t.title }}</v-list-item-title>
            <v-list-item-subtitle>Lista: {{ t.listName }}</v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-menu>
      <v-btn v-if="auth.isAuthenticated" icon>
        <i class="bi bi-person-circle"></i>
      </v-btn>
      <span v-if="auth.user" class="ml-2">{{ auth.user.name }}</span>
      <v-btn v-if="auth.isAuthenticated" @click="logout" variant="text">
        <i class="bi bi-box-arrow-right"></i>
        <span class="ml-1">Sair</span>
      </v-btn>
    </v-app-bar>

    <v-main>
      <router-view />
    </v-main>

    <v-snackbar v-model="ui.snackbar.show" :color="ui.snackbar.color">
      {{ ui.snackbar.message }}
    </v-snackbar>
  </v-app>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import { useTasklistsStore } from '@/stores/useTasklistsStore'
import { useUiStore } from '@/stores/useUiStore'
import { Services } from '@/core/services'

const router = useRouter()
const auth = useAuthStore()
const lists = useTasklistsStore()
const ui = useUiStore()
const drawer = ref(false)
const overdueCount = ref(0)
const overdueTasks = ref<{ id: string; title: string; listId: string; listName: string }[]>([])

onMounted(async () => {
  if (!auth.isAuthenticated) {
    router.push('/login')
    return
  }
  await lists.fetch()
  await updateOverdueCount()
  if (overdueCount.value > 0) {
    ui.showSnackbar(`Você tem ${overdueCount.value} tarefas vencidas.`, 'error')
  }
})

const goToTasks = (listId: string) => {
  router.push({ name: 'tasks', params: { listId } })
  drawer.value = false
}

const logout = () => {
  auth.logout()
  router.push({ name: 'login' })
}

const updateOverdueCount = async () => {
  let count = 0
  const now = new Date()
  const tasksList: { id: string; title: string; listId: string; listName: string }[] = []
  for (const l of lists.items) {
    const fetchedTasks = await Services.tasks.listByListId(l.id)
    const overdue = fetchedTasks.filter(t => !t.completed && t.dueDate && new Date(t.dueDate) < now)
    count += overdue.length
    overdue.forEach(t => tasksList.push({ id: t.id, title: t.title, listId: l.id, listName: l.name }))
  }
  overdueCount.value = count
  overdueTasks.value = tasksList
}

const goToOverdueTask = (task: { id: string; title: string; listId: string; listName: string }) => {
  router.push({ name: 'tasks', params: { listId: task.listId } })
  drawer.value = false
}
</script>
