<template>
  <v-container class="pa-6" style="max-width:420px">
    <v-card>
      <v-card-title>Entrar</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="onSubmit" ref="form">
          <v-text-field
            v-model="email"
            label="Email"
            :rules="[v => !!v || 'Email é obrigatório']"
            required
          />
          <v-text-field
            v-model="password"
            label="Senha"
            type="password"
            :rules="[v => !!v || 'Senha é obrigatória']"
            required
          />
          <v-btn type="submit" block class="mt-2">Entrar</v-btn>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { useRouter } from 'vue-router'

const email = ref('demo@jtech.com')
const password = ref('123456')
const auth = useAuthStore()
const router = useRouter()

const onSubmit = async () => {
  await auth.login(email.value, password.value)
  router.push({ name: 'lists' })
}
</script>
